<template>
  <div>
    <!-- <p style="margin-top: 20px; height: 200px; font-size: 24px !important;">
      {{ $t('wecube_desc') }}
    </p> -->
    <div class="graph_container">
      <div class="wrap">
        <div class="cube">
          <div class="inner-faces inner-face"></div>
          <div class="inner-faces inner-face2"></div>
          <div class="inner-faces inner-face3"></div>
          <div class="outer-faces front">
            <h2>{{ $t('cube_front') }}</h2>
            Workbench 汇聚任务 统一平台
          </div>
          <div class="outer-faces back">
            <h2>{{ $t('cube_back') }}</h2>
            Expectation 规范设计 描绘期望
          </div>
          <div class="outer-faces top">
            <h2>{{ $t('cube_top') }}</h2>
            Equilibration 不断调整 保持对等
          </div>
          <div class="outer-faces bottom">
            <h2>{{ $t('cube_bottom') }}</h2>
            Wisdom 赋予智慧 制定策略
          </div>
          <div class="outer-faces left">
            <h2>{{ $t('cube_left') }}</h2>
            Execution 自动执行 驱动实现
          </div>
          <div class="outer-faces right">
            <h2>{{ $t('cube_right') }}</h2>
            Watching 持续监测 发现差异
          </div>
        </div>
      </div>
    </div>
    <div class="wecube-desc-container">
      <p class="wecube_desc">{{ $t('wecube_desc1') }}</p>
      <p class="wecube_desc">{{ $t('wecube_desc2') }}</p>
      <p class="wecube_desc">{{ $t('wecube_desc3') }}</p>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      currentLanguage: '',
      toggle: true,
      timer: null
    }
  },
  async created () {
    this.getLocalLang()
  },
  methods: {
    getLocalLang () {
      this.currentLanguage = localStorage.getItem('lang') || navigator.language
    }
  }
}
</script>

<style lang="scss" scoped>
.wrap {
  margin-top: 250px;
}
.wecube-desc-container {
  position: absolute;
  height: 180px;
  bottom: 30px;
  margin: 0 auto;
  width: 97%;
}
.wecube_desc {
  margin-top: 10px;
  height: 30px;
  font-size: 24px !important;
  color: white;
  text-align: center;
}
@keyframes bganim {
  from {
    background-position: 0px;
  }

  to {
    background-position: 80px;
  }
}

.cube {
  margin: auto;
  position: relative;
  height: 300px;
  width: 300px;
  transform-style: preserve-3d;
  .inner-faces {
    position: absolute;
    box-sizing: border-box;
    height: 100%;
    width: 100%;
    opacity: 1;
    // transition: transform 0.2s ease-in;
    text-align: center;
    padding: 110px 0;
  }
}

.cube .outer-faces {
  position: absolute;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  opacity: 0.8;
  // background-color: #111;
  background-image: linear-gradient(to right, #64e0f1, #0d5a7b);
  border: solid 2px #83eeff;
  box-shadow: 0px 0px 80px #019ac2;
  color: #fff;
  transition: transform 0.2s ease-in;
  text-align: center;
  padding: 110px 0;
}
.front {
  transform: translateZ(150px);
  animation: front 6s infinite linear;
}

.back {
  transform: translateZ(-150px) rotateY(180deg);
  animation: back 6s infinite linear;
}

.right {
  transform: rotateY(-270deg) translateX(150px);
  transform-origin: top right;
  animation: right 6s infinite linear;
}

.left {
  transform: rotateY(270deg) translateX(-150px);
  transform-origin: center left;
  animation: left 6s infinite linear;
}

.top {
  transform: rotateX(-270deg) translateY(-150px);
  transform-origin: top center;
  animation: top 6s infinite linear;
}

.bottom {
  transform: rotateX(270deg) translateY(150px);
  transform-origin: bottom center;
  animation: bottom 6s infinite linear;
}
.inner-faces {
  background: url('../assets/core_2.png');
  background-position: center;
  background-size: cover;
  opacity: 0.3;
}

.inner-face {
  transform: translate3d(0, 0, 0px);
  animation: innerface 3s infinite linear;
}
.inner-face2 {
  transform: rotateY(90deg) translate3d(0, 0, 0px);
  animation: innerface2 4s infinite linear;
}
.inner-face3 {
  transform: rotateX(90deg) translate3d(0, 0, 0px);
  animation: innerface3 3s infinite linear;
}
@keyframes innerface {
  from {
    transform: rotate(0deg);
    transform: rotateX(0deg) rotateZ(0deg);
  }
  to {
    transform: rotate(360deg);
    transform: rotateX(360deg) rotateZ(360deg);
  }
}
@keyframes innerface2 {
  from {
    transform: rotate(0deg);
    transform: rotateY(0deg) rotateZ(0deg);
  }
  to {
    transform: rotate(360deg);
    transform: rotateY(360deg) rotateZ(360deg);
  }
}
@keyframes innerface3 {
  from {
    transform: rotate(0deg);
    transform: rotateY(0deg) rotateX(0deg);
  }
  to {
    transform: rotate(360deg);
    transform: rotateY(360deg) rotateX(360deg);
  }
}
@keyframes front {
  0% {
    transform: translateZ(150px);
  }
  50% {
    transform: translateZ(250px);
  }
  100% {
    transform: translateZ(150px);
  }
}

@keyframes back {
  0% {
    transform: translateZ(-150px) rotateY(180deg);
  }
  50% {
    transform: translateZ(-250px) rotateY(180deg);
  }
  100% {
    transform: translateZ(-150px) rotateY(180deg);
  }
}

@keyframes right {
  0% {
    transform: rotateY(-270deg) translateX(150px);
    transform-origin: top right;
  }
  50% {
    transform: rotateY(-270deg) translateZ(100px) translateX(150px);
  }
  100% {
    transform: rotateY(-270deg) translateX(150px);
    transform-origin: top right;
  }
}

@keyframes left {
  0% {
    transform: rotateY(270deg) translateX(-150px);
    transform-origin: center left;
  }
  50% {
    transform: rotateY(270deg) translateZ(100px) translateX(-150px);
  }
  100% {
    transform: rotateY(270deg) translateX(-150px);
    transform-origin: center left;
  }
}

@keyframes top {
  0% {
    transform: rotateX(-270deg) translateY(-150px);
    transform-origin: top center;
  }
  50% {
    transform: rotateX(-270deg) translateZ(100px) translateY(-150px);
  }
  100% {
    transform: rotateX(-270deg) translateY(-150px);
    transform-origin: top center;
  }
}

@keyframes bottom {
  0% {
    transform: rotateX(270deg) translateY(150px);
    transform-origin: bottom center;
  }
  50% {
    transform: rotateX(270deg) translateZ(100px) translateY(150px);
  }
  100% {
    transform: rotateX(270deg) translateY(150px);
    transform-origin: bottom center;
  }
}

@keyframes rotate {
  from {
    transform: rotateX(0deg) rotateY(0deg);
  }

  to {
    transform: rotateX(360deg) rotateY(360deg);
  }
}

.cube {
  animation: rotate 20s infinite linear;
}

// .wrap:hover .front {
//   transform: translateZ(250px);
// }

// .wrap:hover .back {
//   transform: translateZ(-250px) rotateY(180deg);
// }

// .wrap:hover .right {
//   transform: rotateY(-270deg) translateZ(100px) translateX(150px);
// }

// .wrap:hover .left {
//   transform: rotateY(270deg) translateZ(100px) translateX(-150px);
// }

// .wrap:hover .top {
//   transform: rotateX(-270deg) translateZ(100px) translateY(-150px);
// }

// .wrap:hover .bottom {
//   transform: rotateX(270deg) translateZ(100px) translateY(150px);
// }
</style>
