package com.travel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.AiChatDTO;
import com.travel.entity.BizHotel;
import com.travel.entity.BizRestaurant;
import com.travel.entity.BizScenicSpot;
import com.travel.vo.AiChatVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI 问答服务（纯云端 DeepSeek + 多轮上下文记忆）：
 * 1. 云端大模型结合对话历史做意图识别 + 关键词抽取（JSON 模式）
 * 2. 关键词在本地数据库召回（酒店/景点/餐厅）
 * 3. 云端大模型结合历史与召回结果生成友好回复，返回可点击卡片
 * <p>
 * 支持三种意图：recommend（找酒店/景点/餐厅）、plan（规划行程）、chat（闲聊）。
 * 提供非流式 {@link #chat} 与流式 {@link #streamChat}（SSE 打字效果）两套接口。
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    /** 最多推荐的卡片数 */
    private static final int MAX_CANDIDATES = 3;
    /** 最多携带的历史消息条数 */
    private static final int MAX_HISTORY = 6;
    /** 单条历史消息最大长度，避免 token 过长 */
    private static final int MAX_HISTORY_CHARS = 150;

    @Value("${travel.ai.base-url}")
    private String baseUrl;

    @Value("${travel.ai.api-key}")
    private String apiKey;

    @Value("${travel.ai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HotelService hotelService;
    private final ScenicSpotService spotService;
    private final RestaurantService restaurantService;

    public AiService(RestTemplate restTemplate, ObjectMapper objectMapper,
                     HotelService hotelService, ScenicSpotService spotService,
                     RestaurantService restaurantService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.hotelService = hotelService;
        this.spotService = spotService;
        this.restaurantService = restaurantService;
    }

    public AiChatVO chat(String userMessage, List<AiChatDTO.History> history) {
        AiChatVO vo = new AiChatVO();
        try {
            // ① 结合历史做意图识别 + 关键词抽取
            IntentResult intent = parseIntent(userMessage, history);

            // ② 行程规划：召回 + 生成按天行程
            if ("plan".equals(intent.intent)) {
                List<AiChatVO.Item> items = retrieve(intent.keywords, intent.category);
                vo.setItems(items);
                vo.setReply(chatContent(buildMessages(planSystem(items), userMessage, history), false));
                return vo;
            }

            // ③ 非推荐意图 → 通用闲聊
            if (!"recommend".equals(intent.intent)) {
                vo.setReply(chatContent(buildMessages(generalSystem(), userMessage, history), false));
                vo.setItems(new ArrayList<>());
                return vo;
            }

            // ④ 关键词召回
            List<AiChatVO.Item> items = retrieve(intent.keywords, intent.category);
            vo.setItems(items);

            // ⑤ 生成回复
            if (items.isEmpty()) {
                vo.setReply(chatContent(buildMessages(noMatchSystem(), userMessage, history), false));
            } else {
                vo.setReply(chatContent(buildMessages(recommendSystem(items), userMessage, history), false));
            }
        } catch (Exception e) {
            log.warn("AI 服务调用失败，返回兜底文案: {}", e.getMessage());
            vo.setReply("抱歉，AI 服务暂时不可用，请稍后再试。");
            vo.setItems(new ArrayList<>());
        }
        return vo;
    }

    /**
     * 流式问答：SSE 逐字返回（打字效果），最后一条事件携带推荐卡片。
     */
    public void streamChat(String userMessage, List<AiChatDTO.History> history, SseEmitter emitter) {
        try {
            IntentResult intent = parseIntent(userMessage, history);

            List<AiChatVO.Item> items = new ArrayList<>();
            String system;
            if ("plan".equals(intent.intent)) {
                items = retrieve(intent.keywords, intent.category);
                system = planSystem(items);
            } else if ("recommend".equals(intent.intent)) {
                items = retrieve(intent.keywords, intent.category);
                system = items.isEmpty() ? noMatchSystem() : recommendSystem(items);
            } else {
                system = generalSystem();
            }

            List<Map<String, String>> messages = buildMessages(system, userMessage, history);
            List<AiChatVO.Item> finalItems = items;
            streamContent(messages, token -> sendEvent(emitter, "token", "content", token));

            // 结束事件：携带可点击卡片
            Map<String, Object> done = new HashMap<>();
            done.put("type", "done");
            done.put("items", finalItems);
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(done)));
            emitter.complete();
        } catch (Exception e) {
            log.warn("AI 流式服务异常: {}", e.getMessage());
            try {
                sendEvent(emitter, "token", "content", "抱歉，AI 服务暂时不可用，请稍后再试。");
                emitter.send(SseEmitter.event().data("{\"type\":\"done\",\"items\":[]}"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private void sendEvent(SseEmitter emitter, String type, String key, String value) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put(key, value);
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(event)));
        } catch (Exception e) {
            throw new RuntimeException("发送 SSE 事件失败", e);
        }
    }

    // ===================== 意图解析 =====================

    private IntentResult parseIntent(String question, List<AiChatDTO.History> history) {
        IntentResult r = new IntentResult();
        r.intent = "chat";
        r.keywords = new ArrayList<>();
        r.category = null;
        try {
            String system = "你是旅游平台的意图解析器。请结合对话上下文，把用户最新这句话解析成 JSON 对象，只输出 JSON，不要输出其它内容。字段：\n"
                    + "intent: \"recommend\"（用户想找酒店/景点/餐厅）或 \"plan\"（用户想规划行程/攻略/几天游，涉及天数、预算或行程安排）或 \"chat\"（其余闲聊）；\n"
                    + "category: \"hotel\"/\"spot\"/\"restaurant\"/null，能判断用户要找哪一类就填，否则 null；\n"
                    + "keywords: 字符串数组，抽取用于检索的关键词（目的地、类型、特色等）。";
            String content = chatContent(buildMessages(system, question, history), true);
            JsonNode node = objectMapper.readTree(extractJson(content));
            if (node.has("intent") && !node.get("intent").isNull()) {
                r.intent = node.get("intent").asText();
            }
            if (node.has("category") && !node.get("category").isNull()) {
                r.category = node.get("category").asText();
            }
            if (node.has("keywords") && node.get("keywords").isArray()) {
                for (JsonNode k : node.get("keywords")) {
                    r.keywords.add(k.asText());
                }
            }
            log.info("AI 意图: intent={}, category={}, keywords={}", r.intent, r.category, r.keywords);
        } catch (Exception e) {
            log.warn("意图解析失败，按闲聊兜底: {}", e.getMessage());
        }
        return r;
    }

    // ===================== 关键词召回 =====================

    private List<AiChatVO.Item> retrieve(List<String> keywords, String category) {
        List<AiChatVO.Item> result = new ArrayList<>();
        if (keywords.isEmpty()) {
            return result;
        }
        boolean wantHotel = category == null || "hotel".equals(category);
        boolean wantSpot = category == null || "spot".equals(category);
        boolean wantRestaurant = category == null || "restaurant".equals(category);

        List<Candidate> candidates = new ArrayList<>();

        if (wantHotel) {
            for (BizHotel h : hotelService.lambdaQuery().eq(BizHotel::getStatus, 1).list()) {
                String text = join(h.getName(), h.getCity(), h.getDistrict(), h.getAddress(), h.getDescription());
                double score = keywordScore(keywords, text);
                if (score > 0) {
                    String sub = h.getCity() + " · " + h.getStarLevel() + "星 · 评分 " + h.getRating();
                    candidates.add(new Candidate(1, h.getId(), h.getName(), sub, score));
                }
            }
        }
        if (wantSpot) {
            for (BizScenicSpot s : spotService.lambdaQuery().eq(BizScenicSpot::getStatus, 1).list()) {
                String text = join(s.getName(), s.getCity(), s.getAddress(), s.getDescription());
                double score = keywordScore(keywords, text);
                if (score > 0) {
                    String sub = s.getCity() + " · 评分 " + s.getRating();
                    candidates.add(new Candidate(2, s.getId(), s.getName(), sub, score));
                }
            }
        }
        if (wantRestaurant) {
            for (BizRestaurant r : restaurantService.lambdaQuery().eq(BizRestaurant::getStatus, 1).list()) {
                String text = join(r.getName(), r.getCity(), r.getAddress(), r.getDescription());
                double score = keywordScore(keywords, text);
                if (score > 0) {
                    String sub = r.getCity() + " · 人均 ¥" + r.getAvgPrice() + " · 评分 " + r.getRating();
                    candidates.add(new Candidate(3, r.getId(), r.getName(), sub, score));
                }
            }
        }

        candidates.sort(Comparator.comparingDouble((Candidate c) -> c.score).reversed());
        for (int i = 0; i < Math.min(MAX_CANDIDATES, candidates.size()); i++) {
            Candidate c = candidates.get(i);
            AiChatVO.Item item = new AiChatVO.Item();
            item.setId(c.id);
            item.setType(c.type);
            item.setName(c.name);
            item.setSub(c.sub);
            result.add(item);
        }
        return result;
    }

    // ===================== 系统提示词 =====================

    private String recommendSystem(List<AiChatVO.Item> items) {
        StringBuilder ctx = new StringBuilder("平台匹配到的内容：\n");
        for (int i = 0; i < items.size(); i++) {
            AiChatVO.Item it = items.get(i);
            ctx.append(i + 1).append(". ").append(it.getName()).append("（").append(it.getSub()).append("）\n");
        }
        return "你是旅游服务平台的 AI 旅行助手。请结合对话上下文，基于下面提供的平台真实数据，"
                + "用友好简洁的语气回答用户，自然地介绍这些内容如何满足用户需求，可对比特点帮助用户选择。"
                + "回答控制在 120 字以内。\n" + ctx;
    }

    private String planSystem(List<AiChatVO.Item> items) {
        StringBuilder ctx = new StringBuilder("平台可推荐的真实内容：\n");
        if (items.isEmpty()) {
            ctx.append("（暂无匹配到本地数据，可基于常识给出行程建议）\n");
        } else {
            for (int i = 0; i < items.size(); i++) {
                AiChatVO.Item it = items.get(i);
                ctx.append(i + 1).append(". ").append(it.getName()).append("（").append(it.getSub()).append("）\n");
            }
        }
        return "你是旅游服务平台的 AI 行程规划师。请根据用户消息里的目的地、天数、预算，"
                + "生成一份按天划分的详细行程安排（用「Day1 / Day2」分天，每天写上午/下午/晚上），"
                + "合理控制总预算，并自然地融入下方平台真实数据中的景点、酒店或餐厅。"
                + "最后附一句预算小结。用清晰、友好的中文，可适当使用换行和短句。"
                + "注意：请用纯文本输出，不要使用任何 Markdown 符号（如 **、#、##、- 等），"
                + "分天标题直接写成「Day1」这样的文字即可，不要加星号或井号。\n" + ctx;
    }

    private String noMatchSystem() {
        return "你是旅游服务平台的 AI 旅行助手。当前平台没有与用户需求匹配的内容，"
                + "请结合上下文友好地告知用户，并建议换个关键词或目的地试试。回答控制在 60 字以内。";
    }

    private String generalSystem() {
        return "你是旅游服务平台的 AI 旅行助手。请结合上下文用友好、热情的语气简短回答，"
                + "如果用户想找酒店、景点或美食，可引导用户说出具体目的地和需求。";
    }

    // ===================== 消息组装 =====================

    private List<Map<String, String>> buildMessages(String system, String user, List<AiChatDTO.History> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", system));
        messages.addAll(buildHistory(history));
        messages.add(msg("user", user));
        return messages;
    }

    private List<Map<String, String>> buildHistory(List<AiChatDTO.History> history) {
        List<Map<String, String>> result = new ArrayList<>();
        if (history == null || history.isEmpty()) {
            return result;
        }
        List<AiChatDTO.History> recent = history.size() > MAX_HISTORY
                ? history.subList(history.size() - MAX_HISTORY, history.size())
                : history;
        for (AiChatDTO.History h : recent) {
            if (h == null || h.getContent() == null || h.getContent().isBlank()) {
                continue;
            }
            String role = "assistant".equals(h.getRole()) ? "assistant" : "user";
            String content = h.getContent();
            if (content.length() > MAX_HISTORY_CHARS) {
                content = content.substring(0, MAX_HISTORY_CHARS);
            }
            result.add(msg(role, content));
        }
        return result;
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    // ===================== 底层调用 =====================

    @SuppressWarnings("unchecked")
    private String chatContent(List<Map<String, String>> messages, boolean jsonMode) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.7);
        if (jsonMode) {
            Map<String, Object> rf = new HashMap<>();
            rf.put("type", "json_object");
            body.put("response_format", rf);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/chat/completions", HttpMethod.POST, entity, Map.class);
        Map<String, Object> respBody = resp.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    /**
     * 流式调用 DeepSeek（stream=true），逐 token 回调。
     */
    private void streamContent(List<Map<String, String>> messages, Consumer<String> onToken) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", true);
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        restTemplate.execute(baseUrl + "/chat/completions", HttpMethod.POST,
                request -> {
                    request.getHeaders().putAll(headers);
                    objectMapper.writeValue(request.getBody(), body);
                },
                response -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data:")) {
                                continue;
                            }
                            String data = line.substring(5).trim();
                            if (data.isEmpty() || "[DONE]".equals(data)) {
                                continue;
                            }
                            try {
                                JsonNode node = objectMapper.readTree(data);
                                JsonNode content = node.path("choices").path(0).path("delta").path("content");
                                if (!content.isMissingNode() && !content.isNull()) {
                                    String token = content.asText();
                                    if (!token.isEmpty()) {
                                        onToken.accept(token);
                                    }
                                }
                            } catch (Exception ignored) {
                                // 忽略无法解析的行
                            }
                        }
                    }
                    return null;
                });
    }

    // ===================== 工具方法 =====================

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        String s = content.trim();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(p);
            }
        }
        return sb.toString();
    }

    private double keywordScore(List<String> keywords, String text) {
        String t = text == null ? "" : text.toLowerCase();
        int hits = 0;
        for (String kw : keywords) {
            String k = kw == null ? "" : kw.trim().toLowerCase();
            if (!k.isEmpty() && t.contains(k)) {
                hits++;
            }
        }
        return keywords.isEmpty() ? 0 : (double) hits / keywords.size();
    }

    // ===================== 内部类 =====================

    private static class IntentResult {
        String intent;
        List<String> keywords;
        String category;
    }

    private static class Candidate {
        int type;
        Long id;
        String name;
        String sub;
        double score;

        Candidate(int type, Long id, String name, String sub, double score) {
            this.type = type;
            this.id = id;
            this.name = name;
            this.sub = sub;
            this.score = score;
        }
    }
}
