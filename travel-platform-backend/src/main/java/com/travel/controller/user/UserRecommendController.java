package com.travel.controller.user;

import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.service.RecommendService;
import com.travel.vo.RecommendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端：个性化推荐（需登录，基于收藏/订单行为的混合推荐）
 */
@RestController
@RequestMapping("/api/user/recommend")
public class UserRecommendController {

    private final RecommendService recommendService;

    public UserRecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    public R<List<RecommendVO.Item>> recommend() {
        return R.ok(recommendService.recommend(UserContext.getUserId()));
    }
}
