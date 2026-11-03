import { BASE_URL } from '../config/index.js'
import store from '../store/index.js'

/**
 * H5 流式问答客户端：用 fetch + ReadableStream 消费后端 SSE（打字效果）。
 * 后端事件格式：data: {"type":"token","content":"..."} 与 data: {"type":"done","items":[...]}
 *
 * 仅 H5 可用；其它端请在调用处用 #ifdef 条件编译降级为非流式 aiChat。
 *
 * @param {Object} payload  { message, history }
 * @param {Object} handlers { onToken(content), onDone(items) }
 */
export async function aiChatStream(payload, { onToken, onDone } = {}) {
  // #ifdef H5
  const token = store.getToken()
  const res = await fetch(BASE_URL + '/api/user/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify(payload)
  })

  if (!res.ok || !res.body) {
    throw new Error('流式请求失败：' + res.status)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    let idx
    while ((idx = buffer.indexOf('\n\n')) >= 0) {
      const raw = buffer.slice(0, idx)
      buffer = buffer.slice(idx + 2)
      const data = raw
        .split('\n')
        .filter((l) => l.startsWith('data:'))
        .map((l) => l.slice(5).trim())
        .join('')
      if (!data) continue

      let evt
      try {
        evt = JSON.parse(data)
      } catch (e) {
        continue
      }
      if (evt.type === 'token' && onToken) {
        onToken(evt.content || '')
      } else if (evt.type === 'done' && onDone) {
        onDone(evt.items || [])
      }
    }
  }
  // #endif
}
