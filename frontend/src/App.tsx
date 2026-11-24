import { useState } from 'react'
import { 
  ShoppingBag, 
  Store, 
  MapPin, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Sparkles
} from 'lucide-react'

// Types aligned with 04-modele-de-donnees.md
interface Product {
  id: string
  name: string
  price: number
  photoUrl: string
  availableStock: number
}

interface Vendor {
  id: string
  shopName: string
  ownerName: string
  isPresentToday: boolean
  products: Product[]
}

interface OrderItem {
  id: string
  customerName: string
  auditoire: string
  productName: string
  quantity: number
  unitPrice: number
  status: 'EN_ATTENTE' | 'LIVREE_PAYEE' | 'ANNULEE'
  time: string
}

const INITIAL_VENDORS: Vendor[] = [
  {
    id: 'v1',
    shopName: 'Les Délices de Sarah',
    ownerName: 'Sarah M.',
    isPresentToday: true,
    products: [
      { id: 'p1', name: 'Sandwich Poulet Mayo', price: 3500, photoUrl: '🥪', availableStock: 8 },
      { id: 'p2', name: 'Jus d’Ananas Frais (33cl)', price: 2000, photoUrl: '🍍', availableStock: 5 },
      { id: 'p3', name: 'Gaufre Dorée Sucrée', price: 1500, photoUrl: '🧇', availableStock: 12 }
    ]
  },
  {
    id: 'v2',
    shopName: 'K-Snack Campus',
    ownerName: 'Kevin T.',
    isPresentToday: true,
    products: [
      { id: 'p4', name: 'Beignets Soufflés (Lot de 3)', price: 1000, photoUrl: '🍩', availableStock: 15 },
      { id: 'p5', name: 'Boisson Fraîche Malt', price: 2500, photoUrl: '🥤', availableStock: 6 }
    ]
  }
]

export function App() {
  const [activeTab, setActiveTab] = useState<'BUYER' | 'VENDOR'>('BUYER')
  const [currentAuditoire, setCurrentAuditoire] = useState('N2 - Auditoire L3 Génie Logiciel')
  const [vendors] = useState<Vendor[]>(INITIAL_VENDORS)
  const [vendorIsPresent, setVendorIsPresent] = useState(true)

  // Orders for vendor view
  const [orders, setOrders] = useState<OrderItem[]>([
    {
      id: 'cmd-101',
      customerName: 'Marc Kabasela',
      auditoire: 'N2 - Auditoire L3 Génie Logiciel',
      productName: 'Sandwich Poulet Mayo',
      quantity: 1,
      unitPrice: 3500,
      status: 'EN_ATTENTE',
      time: '09:42'
    },
    {
      id: 'cmd-102',
      customerName: 'Christelle Mwanza',
      auditoire: 'N1 - Auditoire L1 Économie',
      productName: 'Jus d’Ananas Frais (33cl)',
      quantity: 2,
      unitPrice: 2000,
      status: 'EN_ATTENTE',
      time: '09:48'
    }
  ])

  // Simple cart simulation
  const [cartCount, setCartCount] = useState<Record<string, number>>({})
  const [confirmedMessage, setConfirmedMessage] = useState<string | null>(null)

  const handleQuantityChange = (productId: string, delta: number) => {
    setCartCount(prev => {
      const current = prev[productId] || 0
      const next = Math.max(0, current + delta)
      return { ...prev, [productId]: next }
    })
  }

  const handleOrder = (product: Product, shopName: string) => {
    const qty = cartCount[product.id] || 1
    const newOrder: OrderItem = {
      id: `cmd-${Date.now().toString().slice(-3)}`,
      customerName: 'Emmanuel Musasa (Vous)',
      auditoire: currentAuditoire,
      productName: product.name,
      quantity: qty,
      unitPrice: product.price,
      status: 'EN_ATTENTE',
      time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
    }
    setOrders(prev => [newOrder, ...prev])
    setCartCount(prev => ({ ...prev, [product.id]: 0 }))
    setConfirmedMessage(`Commande de ${qty}x ${product.name} envoyée à ${shopName} ! Livraison prévue à votre auditoire.`)
    setTimeout(() => setConfirmedMessage(null), 5000)
  }

  const handleUpdateOrderStatus = (orderId: string, status: 'LIVREE_PAYEE' | 'ANNULEE') => {
    setOrders(prev =>
      prev.map(o => (o.id === orderId ? { ...o, status } : o))
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-12">
      {/* Top Bar */}
      <header className="sticky top-0 z-30 bg-white/90 backdrop-blur-md border-b border-slate-200">
        <div className="max-w-md mx-auto px-4 py-3 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5">
              <span className="text-xl font-black tracking-tight text-black">Proximo</span>
              <span className="text-xs font-bold uppercase tracking-wider bg-black text-white px-1.5 py-0.5 rounded">UCC</span>
            </div>
            <p className="text-[11px] text-slate-500 font-medium">Campus Limete & Mont-Ngafula</p>
          </div>

          {/* Mode Switcher */}
          <div className="flex bg-slate-100 p-1 rounded-full border border-slate-200 text-xs font-semibold">
            <button
              onClick={() => setActiveTab('BUYER')}
              className={`px-3 py-1 rounded-full transition-all flex items-center gap-1 ${
                activeTab === 'BUYER' ? 'bg-black text-white shadow-sm' : 'text-slate-600 hover:text-black'
              }`}
            >
              <ShoppingBag className="w-3.5 h-3.5" />
              Acheter
            </button>
            <button
              onClick={() => setActiveTab('VENDOR')}
              className={`px-3 py-1 rounded-full transition-all flex items-center gap-1 ${
                activeTab === 'VENDOR' ? 'bg-black text-white shadow-sm' : 'text-slate-600 hover:text-black'
              }`}
            >
              <Store className="w-3.5 h-3.5" />
              Vendre
            </button>
          </div>
        </div>

        {/* Location & Time Status */}
        <div className="max-w-md mx-auto px-4 py-2 bg-slate-100/70 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
          <div className="flex items-center gap-1.5 truncate">
            <MapPin className="w-3.5 h-3.5 text-black shrink-0" />
            <select
              value={currentAuditoire}
              onChange={e => setCurrentAuditoire(e.target.value)}
              className="bg-transparent font-medium text-black focus:outline-none cursor-pointer truncate"
            >
              <option value="N2 - Auditoire L3 Génie Logiciel">N2 - Auditoire L3 Génie Logiciel</option>
              <option value="N1 - Auditoire L1 Économie">N1 - Auditoire L1 Économie</option>
              <option value="N1 - Auditoire L2 Droit">N1 - Auditoire L2 Droit</option>
              <option value="N3 - Auditoire Master Finance">N3 - Auditoire Master Finance</option>
            </select>
          </div>
          <div className="flex items-center gap-1 text-[11px] text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200 shrink-0 font-medium">
            <Clock className="w-3 h-3" />
            <span>Fermeture 15h30</span>
          </div>
        </div>
      </header>

      {/* Main Container (Mobile Form Factor) */}
      <main className="max-w-md mx-auto px-4 pt-4">
        {confirmedMessage && (
          <div className="mb-4 p-3 bg-emerald-50 border border-emerald-300 rounded-xl flex items-start gap-2.5 shadow-sm text-emerald-900 text-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-xs text-emerald-800">Commande enregistrée !</p>
              <p className="text-xs text-emerald-700 mt-0.5">{confirmedMessage}</p>
            </div>
          </div>
        )}

        {/* ================= BUYER VIEW ================= */}
        {activeTab === 'BUYER' && (
          <div className="space-y-6">
            {/* Banner info */}
            <div className="bg-black text-white p-4 rounded-2xl shadow-sm">
              <div className="flex items-center gap-2 mb-1">
                <Sparkles className="w-4 h-4 text-amber-300" />
                <h2 className="text-sm font-bold uppercase tracking-wider text-slate-200">Livraison à votre place</h2>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                Commandez vos collations auprès des vendeurs présents. Le vendeur vous livre directement dans votre auditoire avec paiement en cash.
              </p>
            </div>

            {/* Vendors List */}
            <div className="space-y-4">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">
                Vendeurs présents aujourd'hui ({vendors.filter(v => v.isPresentToday).length})
              </h3>

              {vendors.map(vendor => (
                <div key={vendor.id} className="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm space-y-3">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <div>
                      <h4 className="font-bold text-base text-slate-900">{vendor.shopName}</h4>
                      <p className="text-xs text-slate-500">Tenu par {vendor.ownerName}</p>
                    </div>
                    <span className="text-[11px] font-semibold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse"></span>
                      Présent
                    </span>
                  </div>

                  {/* Products */}
                  <div className="space-y-2">
                    {vendor.products.map(product => {
                      const qty = cartCount[product.id] || 1
                      return (
                        <div key={product.id} className="flex items-center justify-between p-2.5 rounded-xl hover:bg-slate-50 border border-slate-100">
                          <div className="flex items-center gap-3">
                            <span className="text-2xl p-1 bg-slate-100 rounded-lg">{product.photoUrl}</span>
                            <div>
                              <p className="font-semibold text-sm text-slate-800">{product.name}</p>
                              <p className="text-xs font-bold text-black">{product.price.toLocaleString()} FC</p>
                              <p className="text-[10px] text-slate-400">Stock restant : {product.availableStock}</p>
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            <div className="flex items-center border border-slate-200 rounded-lg bg-white overflow-hidden text-xs">
                              <button 
                                onClick={() => handleQuantityChange(product.id, -1)}
                                className="px-2 py-1 text-slate-600 hover:bg-slate-100 font-bold"
                              >
                                -
                              </button>
                              <span className="px-2 font-bold text-black">{qty}</span>
                              <button 
                                onClick={() => handleQuantityChange(product.id, 1)}
                                className="px-2 py-1 text-slate-600 hover:bg-slate-100 font-bold"
                              >
                                +
                              </button>
                            </div>

                            <button
                              onClick={() => handleOrder(product, vendor.shopName)}
                              className="bg-black hover:bg-slate-800 text-white text-xs font-semibold px-3 py-1.5 rounded-xl transition-all shadow-sm active:scale-95"
                            >
                              Commander
                            </button>
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ================= VENDOR VIEW ================= */}
        {activeTab === 'VENDOR' && (
          <div className="space-y-5">
            {/* Daily Presence Switch */}
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm text-slate-900">Statut du jour</h3>
                <p className="text-xs text-slate-500">Indiquer votre présence active aux étudiants</p>
              </div>
              <button
                onClick={() => setVendorIsPresent(!vendorIsPresent)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shadow-sm ${
                  vendorIsPresent ? 'bg-emerald-600 text-white' : 'bg-slate-200 text-slate-700'
                }`}
              >
                {vendorIsPresent ? '✓ Je suis là aujourd’hui' : '✕ Absent aujourd’hui'}
              </button>
            </div>

            {/* Orders Grouped by Auditoire */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">
                  Commandes à livrer ({orders.filter(o => o.status === 'EN_ATTENTE').length})
                </h3>
                <span className="text-[11px] text-slate-400">Triées par auditoire</span>
              </div>

              {orders.length === 0 ? (
                <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-400 text-xs">
                  Aucune commande pour le moment
                </div>
              ) : (
                orders.map(order => (
                  <div 
                    key={order.id} 
                    className={`bg-white rounded-2xl p-4 border transition-all shadow-sm space-y-3 ${
                      order.status === 'EN_ATTENTE' ? 'border-amber-300 ring-1 ring-amber-100' :
                      order.status === 'LIVREE_PAYEE' ? 'border-slate-200 opacity-60' : 'border-rose-200 opacity-60'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <div className="flex items-center gap-1.5">
                          <MapPin className="w-3.5 h-3.5 text-black" />
                          <span className="font-bold text-xs text-black">{order.auditoire}</span>
                        </div>
                        <p className="text-xs text-slate-600 mt-0.5">Client : <strong className="text-slate-800">{order.customerName}</strong></p>
                      </div>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        order.status === 'EN_ATTENTE' ? 'bg-amber-100 text-amber-900' :
                        order.status === 'LIVREE_PAYEE' ? 'bg-emerald-100 text-emerald-900' : 'bg-rose-100 text-rose-900'
                      }`}>
                        {order.status === 'EN_ATTENTE' ? 'En attente' :
                         order.status === 'LIVREE_PAYEE' ? 'Payé & Livré' : 'Annulé'}
                      </span>
                    </div>

                    <div className="bg-slate-50 p-2.5 rounded-xl flex items-center justify-between text-xs">
                      <span>{order.quantity}x {order.productName}</span>
                      <strong className="text-black font-mono">{(order.quantity * order.unitPrice).toLocaleString()} FC (Cash)</strong>
                    </div>

                    {order.status === 'EN_ATTENTE' && (
                      <div className="flex items-center gap-2 pt-1">
                        <button
                          onClick={() => handleUpdateOrderStatus(order.id, 'LIVREE_PAYEE')}
                          className="flex-1 bg-black hover:bg-slate-800 text-white text-xs font-semibold py-2 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                          Payé (Cash reçu)
                        </button>
                        <button
                          onClick={() => handleUpdateOrderStatus(order.id, 'ANNULEE')}
                          className="bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-600 text-xs font-semibold px-3 py-2 rounded-xl transition-all border border-slate-200"
                        >
                          <XCircle className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default App
