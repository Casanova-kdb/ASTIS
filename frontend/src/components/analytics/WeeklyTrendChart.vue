<template>
  <div class="trend-chart" :aria-label="ariaLabel">
    <Line v-if="chartType === 'line'" :data="chartData" :options="chartOptions" />
    <Bar v-else :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip
} from 'chart.js'
import { Bar, Line } from 'vue-chartjs'

ChartJS.register(
  BarElement,
  CategoryScale,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip
)

const props = defineProps({
  labels: {
    type: Array,
    required: true
  },
  values: {
    type: Array,
    required: true
  },
  datasetLabel: {
    type: String,
    required: true
  },
  chartType: {
    type: String,
    default: 'line'
  },
  color: {
    type: String,
    required: true
  },
  ariaLabel: {
    type: String,
    required: true
  }
})

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [
    {
      label: props.datasetLabel,
      data: props.values,
      backgroundColor: props.chartType === 'line' ? `${props.color}26` : `${props.color}bf`,
      borderColor: props.color,
      borderRadius: props.chartType === 'bar' ? 4 : 0,
      borderWidth: 2,
      fill: props.chartType === 'line',
      pointBackgroundColor: props.color,
      pointRadius: 3,
      pointHoverRadius: 5,
      tension: 0.3
    }
  ]
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    intersect: false,
    mode: 'index'
  },
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      displayColors: false
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      ticks: {
        color: '#52616f',
        maxRotation: 0
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        color: '#e6eef5'
      },
      ticks: {
        color: '#52616f',
        precision: 0,
        stepSize: 1
      }
    }
  }
}))
</script>
