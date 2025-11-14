import { ref } from 'vue'

export interface Toast {
  id: string
  message: string
  type: 'success' | 'error' | 'info' | 'warning'
  duration?: number
}

const toasts = ref<Toast[]>([])

export function useToast() {
  function showToast(message: string, type: Toast['type'] = 'info', duration: number = 5000) {
    // Remove toasts anteriores do mesmo tipo (especialmente erros)
    if (type === 'error') {
      toasts.value = toasts.value.filter(t => t.type !== 'error')
    }

    const id = Math.random().toString(36).substring(2, 9)
    const toast: Toast = {
      id,
      message,
      type,
      duration
    }

    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }

    return id
  }

  function removeToast(id: string) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  function success(message: string, duration?: number) {
    return showToast(message, 'success', duration)
  }

  function error(message: string, duration?: number) {
    return showToast(message, 'error', duration)
  }

  function info(message: string, duration?: number) {
    return showToast(message, 'info', duration)
  }

  function warning(message: string, duration?: number) {
    return showToast(message, 'warning', duration)
  }

  return {
    toasts,
    showToast,
    removeToast,
    success,
    error,
    info,
    warning
  }
}

