<template>
  <div>
    <div v-show="!!data" class="graph-container" id="auth-setting-graph"></div>
    <span v-show="!data">{{ $t('no_auth_provided') }}</span>
  </div>
</template>
<script>
import { getAuthSettings } from '@/api/server'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
export default {
  name: 'auth-setting',
  data () {
    return {
      data: [],
      graph: {}
    }
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData()
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  created () {
    this.getData()
  },
  methods: {
    async getData () {
      let { status, data } = await getAuthSettings(this.pkgId)

      if (status === 'OK') {
        let allRoles = []
        let allMenus = []
        allRoles = data.map(_ => _.roleName)
        allMenus = data.map(_ => _.menuCode)
        let roles = Array.from(new Set(allRoles))
        let menus = Array.from(new Set(allMenus))

        this.data = {
          roles,
          menus,
          path: data
        }
        this.initGraph()
      }
    },

    renderGraph () {
      let genEdge = () => {
        let pathAry = this.data.path.map(_ => {
          return _.roleName + ' -> ' + _.menuCode
        })
        return pathAry.toString().replace(/,/g, ';')
      }
      let nodesString =
        `digraph G { rankdir = LR;
                          subgraph cluster_0 {
                            rankdir = TB;
                            color=lightgrey;
                            node [shape=box,fontname=Arial, style=filled,color=lightgrey];` +
        this.data.roles.toString().replace(/,/g, ' ') +
        `;
                            label = "Roles";
                          }
                          subgraph cluster_1 {
                            color=lightgrey;
                            node [shape=box,fontname=Arial, style=filled,color=lightgrey];` +
        this.data.menus.toString().replace(/,/g, ' ') +
        `;
                            label = "Menus";
                          }` +
        genEdge() +
        `}`

      this.graph.graphviz.renderDot(nodesString)
    },
    initGraph () {
      const initEvent = () => {
        let graph
        graph = d3.select(`#auth-setting-graph`)
        graph.on('dblclick.zoom', null)
        this.graph.graphviz = graph
          .graphviz()
          .zoom(false)
          .scale(0.8)
      }

      initEvent()
      this.renderGraph()
    }
  }
}
</script>
