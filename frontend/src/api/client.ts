// API Client for Proximo UCC Backend

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export interface UserDTO {
  token?: string
  tokenType?: string
  userId: number
  fullName: string
  email: string
  role: 'STUDENT' | 'VENDOR' | 'ADMIN'
  defaultAuditoireId?: number
  defaultAuditoireCode?: string
  shopId?: number
  shopName?: string
}

export interface AuditoireDTO {
  id: number
  code: string
  name: string
  promotion?: string
  level: number
  positionOrder?: number
  side?: string
  posX?: number
  posY?: number
}

export interface ProductDTO {
  id: number
  shopId: number
  name: string
  price: number
  photoUrl?: string
  active: boolean
  availableStock: number
  stockQuantity: number
  reservedQuantity: number
}

export interface ShopDTO {
  id: number
  ownerId: number
  ownerName: string
  name: string
  description?: string
  imageUrl?: string
  active: boolean
  presentToday: boolean
  products: ProductDTO[]
}

export interface OrderItemDTO {
  id: number
  productId: number
  productName: string
  photoUrl?: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface OrderDTO {
  id: number
  buyerId: number
  buyerName: string
  shopId: number
  shopName: string
  auditoireId: number
  auditoireCode: string
  auditoireName: string
  auditoireLevel: number
  auditoirePosition: number
  status: 'EN_ATTENTE' | 'LIVREE_PAYEE' | 'ANNULEE' | 'EXPIREE'
  cancellationReason?: string
  totalAmount: number
  createdAt: string
  updatedAt: string
  items: OrderItemDTO[]
}

class ApiService {
  private getToken(): string | null {
    return localStorage.getItem('proximo_token')
  }

  public setToken(token: string) {
    localStorage.setItem('proximo_token', token)
  }

  public clearToken() {
    localStorage.removeItem('proximo_token')
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const token = this.getToken()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const res = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    })

    if (!res.ok) {
      let errorMsg = `Erreur HTTP ${res.status}`
      try {
        const body = await res.json()
        errorMsg = body.message || body.error || errorMsg
      } catch {
        // use default error message
      }
      throw new Error(errorMsg)
    }

    if (res.status === 204) {
      return {} as T
    }

    return res.json()
  }

  // Auth
  async register(data: { fullName: string; email: string; phone?: string; password: string; defaultAuditoireId?: number }): Promise<UserDTO> {
    const res = await this.request<UserDTO>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    if (res.token) this.setToken(res.token)
    return res
  }

  async login(data: { email: string; password: string }): Promise<UserDTO> {
    const res = await this.request<UserDTO>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    if (res.token) this.setToken(res.token)
    return res
  }

  async getMe(): Promise<UserDTO> {
    return this.request<UserDTO>('/auth/me')
  }

  // Auditoires
  async getAuditoires(): Promise<AuditoireDTO[]> {
    return this.request<AuditoireDTO[]>('/auditoires')
  }

  // Shops
  async getPresentShopsToday(): Promise<ShopDTO[]> {
    return this.request<ShopDTO[]>('/shops/today')
  }

  async getMyShop(): Promise<ShopDTO> {
    return this.request<ShopDTO>('/shops/my-shop')
  }

  async createShop(data: { name: string; description?: string; imageUrl?: string }): Promise<ShopDTO> {
    return this.request<ShopDTO>('/shops', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }

  async togglePresence(present: boolean): Promise<boolean> {
    return this.request<boolean>(`/shops/presence?present=${present}`, {
      method: 'POST',
    })
  }

  async declareStock(declarations: { productId: number; stockQuantity: number }[]): Promise<ProductDTO[]> {
    return this.request<ProductDTO[]>('/shops/stock', {
      method: 'POST',
      body: JSON.stringify(declarations),
    })
  }

  // Products
  async createProduct(data: { name: string; price: number; photoUrl?: string }): Promise<ProductDTO> {
    return this.request<ProductDTO>('/products', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }

  // Orders
  async createOrder(data: { shopId: number; auditoireId: number; items: { productId: number; quantity: number }[] }): Promise<OrderDTO> {
    return this.request<OrderDTO>('/orders', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }

  async getMyOrders(): Promise<OrderDTO[]> {
    return this.request<OrderDTO[]>('/orders/my-orders')
  }

  async getVendorActiveOrders(): Promise<OrderDTO[]> {
    return this.request<OrderDTO[]>('/orders/vendor/active')
  }

  async confirmOrderPayment(orderId: number): Promise<OrderDTO> {
    return this.request<OrderDTO>(`/orders/${orderId}/confirm-paid`, {
      method: 'POST',
    })
  }

  async cancelOrder(orderId: number, reason?: string): Promise<OrderDTO> {
    const url = reason ? `/orders/${orderId}/cancel?reason=${encodeURIComponent(reason)}` : `/orders/${orderId}/cancel`
    return this.request<OrderDTO>(url, {
      method: 'POST',
    })
  }
}

export const api = new ApiService()
