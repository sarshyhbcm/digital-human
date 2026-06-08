import { ref } from 'vue'

export const showAuthDialog = ref(false)
export const pendingRoute = ref(null)

export function openAuthDialog(route) {
  pendingRoute.value = route
  showAuthDialog.value = true
}

export function closeAuthDialog() {
  showAuthDialog.value = false
  pendingRoute.value = null
}
