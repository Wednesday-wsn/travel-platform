<script>
import { refreshOrderBadge } from './utils/badge.js'

export default {
  onLaunch: function () {
    // 应用启动时恢复登录态（token / userInfo 已在 storage 中，store 读取时自动带出）
  },
  onShow: function () {
    // 延迟到 tabBar 渲染完成后再刷新订单角标
    setTimeout(() => refreshOrderBadge(), 200)
  },
  onHide: function () {}
}
</script>

<style lang="scss">
/* 图鸟图标库（tn-icon-*，base64 内嵌字体，747 个矢量图标） */
@import '@/static/iconfont/tuniao-iconfont.css';

/* 全局通用样式 */
page {
  background-color: #F4F2EB;
  color: #33382F;
  font-size: 28rpx;
}

/* 通用卡片 */
.card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin: 20rpx 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 41, 55, 0.06);
}

/* 通用主按钮 */
.btn-primary {
  background-color: #2F4F45;
  color: #ffffff;
  border-radius: 44rpx;
  text-align: center;
  line-height: 88rpx;
  height: 88rpx;
  font-size: 30rpx;
}

/* 价格 */
.price {
  color: #A63D40;
  font-weight: 600;
}

/* 单行省略 */
.ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ===== 渐变工具类（江南园林色板：竹青/石青/藕荷/朱砂/秋香） ===== */
.g-blue { background-image: linear-gradient(135deg, #4A6B5F, #2F4F45); }
.g-cyan { background-image: linear-gradient(135deg, #5A7A6B, #3A5F61); }
.g-green { background-image: linear-gradient(135deg, #7A9A6B, #4C6B4A); }
.g-purple { background-image: linear-gradient(135deg, #8A7A9A, #5A4A6B); }
.g-pink { background-image: linear-gradient(135deg, #C08A86, #9A5F5B); }
.g-orange { background-image: linear-gradient(135deg, #B06A4A, #8A4A38); }
.g-sunset { background-image: linear-gradient(135deg, #C9A05A, #D6BC7A); }

/* 头部主渐变（灰绿→黛青墨，水墨江南） */
.g-primary {
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 45%, #22352F 100%);
}

/* 月洞门：圆形门洞，带一圈内环 */
.moon-gate {
  border-radius: 50%;
  position: relative;
  overflow: hidden;
}
.moon-gate::after {
  content: '';
  position: absolute;
  left: 8rpx;
  top: 8rpx;
  right: 8rpx;
  bottom: 8rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.35);
  pointer-events: none;
}

/* 漏窗/花窗纹样（回字纹），作装饰覆盖层 */
.garden-overlay {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='64' height='64' viewBox='0 0 64 64'%3E%3Cpath d='M12 12 H52 V52 H12 Z' fill='none' stroke='rgba(255,255,255,0.14)' stroke-width='1.5'/%3E%3Cpath d='M26 26 H38 V38 H26 Z' fill='none' stroke='rgba(255,255,255,0.14)' stroke-width='1.5'/%3E%3C/svg%3E");
}

/* 彩色圆形图标徽章 */
.icon-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  color: #ffffff;
  font-size: 36rpx;
  flex-shrink: 0;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.12);
}
</style>
