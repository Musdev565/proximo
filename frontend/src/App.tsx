import { useState, useEffect } from 'react'
import { 
  ShoppingBag, 
  Store, 
  MapPin, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Sparkles,
  Map as MapIcon,
  User as UserIcon,
  LogOut
} from 'lucide-react'
import { api, type AuditoireDTO, type ShopDTO, type OrderDTO, type UserDTO } from './api/client'
import { AuditoireMap2D } from './components/AuditoireMap2D'

// Default fallback auditoires matching V2 migration
const DEFAULT_AUDITOIRES: AuditoireDTO[] = [
  { id: 1, code: 'N1-A01', name: 'Auditoire Jean-Paul II', promotion: 'L1 Sciences Informatiques', level: 1, positionOrder: 1, side: 'gauche' },
  { id: 2, code: 'N1-A02', name: 'Auditoire Cardinal Malula', promotion: 'L1 Droit', level: 1, positionOrder: 2, side: 'gauche' },
  { id: 3, code: 'N1-A03', name: 'Auditoire Mgr Tshibangu', promotion: 'L1 Sciences Économiques', level: 1, positionOrder: 3, side: 'droite' },
  { id: 4, code: 'N2-A01', name: 'Auditoire Thomas d\'Aquin', promotion: 'L2 Génie Logiciel', level: 2, positionOrder: 1, side: 'gauche' },
  { id: 5, code: 'N2-A02', name: 'Auditoire Saint Augustin', promotion: 'L2 Réseaux & Télécoms', level: 2, positionOrder: 2, side: 'gauche' },
  { id: 6, code: 'N2-A03', name: 'Auditoire Mère Teresa', promotion: 'L2 Droit Privé', level: 2, positionOrder: 3, side: 'droite' },
  { id: 7, code: 'N3-A01', name: 'Auditoire Benoît XVI', promotion: 'L3 Génie Logiciel', level: 3, positionOrder: 1, side: 'gauche' },
  { id: 8, code: 'N3-A02', name: 'Auditoire François', promotion: 'Master 1 IA & Big Data', level: 3, positionOrder: 2, side: 'droite' }
]

const DEFAULT_SHOPS: ShopDTO[] = [
  {
    id: 1,
    ownerId: 2,
    ownerName: 'Sarah Mukendi',
    name: 'Les Délices de Sarah',
    description: 'Sandwiches frais et jus naturels faits maison',
    active: true,
    presentToday: true,
    products: [
      { id: 101, shopId: 1, name: 'Sandwich Poulet Mayo', price: 3500, photoUrl: '🥪', active: true, availableStock: 8, stockQuantity: 10, reservedQuantity: 2 },
      { id: 102, shopId: 1, name: 'Jus d’Ananas Frais (33cl)', price: 2000, photoUrl: '🍍', active: true, availableStock: 5, stockQuantity: 6, reservedQuantity: 1 },
      { id: 103, shopId: 1, name: 'Gaufre Dorée Sucrée', price: 1500, photoUrl: '🧇', active: true, availableStock: 12, stockQuantity: 12, reservedQuantity: 0 }
    ]
  },
  {
    id: 2,
    ownerId: 3,
    ownerName: 'Kevin Tshilumba',
    name: 'K-Snack Campus',
    description: 'Beignets chauds et boissons fraîches',
    active: true,
    presentToday: true,
    products: [
      { id: 104, shopId: 2, name: 'Beignets Soufflés (Lot de 3)', price: 1000, photoUrl: '🍩', active: true, availableStock: 15, stockQuantity: 15, reservedQuantity: 0 },
      { id: 105, shopId: 2, name: 'Boisson Fraîche Malt', price: 2500, photoUrl: '🥤', active: true, availableStock: 6, stockQuantity: 6, reservedQuantity: 0 }
    ]
  }
]

export function App() {
  const [activeTab, setActiveTab] = useState<'BUYER' | 'VENDOR' | 'MAP'>('BUYER')
  const [auditoires, setAuditoires] = useState<AuditoireDTO[]>(DEFAULT_AUDITOIRES)
  const [selectedAuditoireId, setSelectedAuditoireId] = useState<number>(4)
  const [shops, setShops] = useState<ShopDTO[]>(DEFAULT_SHOPS)
  const [vendorIsPresent, setVendorIsPresent] = useState(true)
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null)
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [isRegisterMode, setIsRegisterMode] = useState(false)
  const [authEmail, setAuthEmail] = useState('')
  const [authPassword, setAuthPassword] = useState('')
  const [authFullName, setAuthFullName] = useState('')

  // Orders for vendor view
  const [orders, setOrders] = useState<OrderDTO[]>([
    {
      id: 101,
      buyerId: 1,
      buyerName: 'Marc Kabasela',
      shopId: 1,
      shopName: 'Les Délices de Sarah',
      auditoireId: 4,
      auditoireCode: 'N2-A01',
      auditoireName: 'Auditoire Thomas d\'Aquin',
      auditoireLevel: 2,
      auditoirePosition: 1,
      status: 'EN_ATTENTE',
      totalAmount: 3500,
      createdAt: '2026-11-24T09:42:00',
      updatedAt: '2026-11-24T09:42:00',
      items: [{ id: 1, productId: 101, productName: 'Sandwich Poulet Mayo', quantity: 1, unitPrice: 3500, subtotal: 3500 }]
    },
    {
      id: 102,
      buyerId: 2,
      buyerName: 'Christelle Mwanza',
      shopId: 1,
      shopName: 'Les Délices de Sarah',
      auditoireId: 1,
      auditoireCode: 'N1-A01',
      auditoireName: 'Auditoire Jean-Paul II',
      auditoireLevel: 1,
      auditoirePosition: 1,
      status: 'EN_ATTENTE',
      totalAmount: 4000,
      createdAt: '2026-11-24T09:48:00',
      updatedAt: '2026-11-24T09:48:00',
      items: [{ id: 2, productId: 102, productName: 'Jus d’Ananas Frais (33cl)', quantity: 2, unitPrice: 2000, subtotal: 4000 }]
    }
  ])

  // Cart & notifications
  const [cartCount, setCartCount] = useState<Record<number, number>>({})
  const [confirmedMessage, setConfirmedMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // Load backend data if available
  useEffect(() => {
    api.getAuditoires()
      .then(data => {
        if (data && data.length > 0) setAuditoires(data)
      })
      .catch(() => {
        // Fallback to default auditoires
      })

    api.getPresentShopsToday()
      .then(data => {
        if (data && data.length > 0) setShops(data)
      })
      .catch(() => {
        // Fallback to default shops
      })

    api.getMe()
      .then(user => setCurrentUser(user))
      .catch(() => {
        // Not logged in
      })
  }, [])

  const selectedAuditoire = auditoires.find(a => a.id === selectedAuditoireId) || auditoires[0]

  const handleQuantityChange = (productId: number, delta: number) => {
    setCartCount(prev => {
      const current = prev[productId] || 0
      const next = Math.max(0, current + delta)
      return { ...prev, [productId]: next }
    })
  }

  const handleOrder = async (productId: number, shopId: number, productName: string, price: number, shopName: string) => {
    const qty = cartCount[productId] || 1

    try {
      if (currentUser) {
        await api.createOrder({
          shopId,
          auditoireId: selectedAuditoire.id,
          items: [{ productId, quantity: qty }]
        })
      }

      // Local optimistic update
      const newOrder: OrderDTO = {
        id: Date.now(),
        buyerId: currentUser ? currentUser.userId : 1,
        buyerName: currentUser ? currentUser.fullName : 'Emmanuel Musasa (Vous)',
        shopId,
        shopName,
        auditoireId: selectedAuditoire.id,
        auditoireCode: selectedAuditoire.code,
        auditoireName: selectedAuditoire.name,
        auditoireLevel: selectedAuditoire.level,
        auditoirePosition: selectedAuditoire.positionOrder || 1,
        status: 'EN_ATTENTE',
        totalAmount: qty * price,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        items: [{ id: Date.now(), productId, productName, quantity: qty, unitPrice: price, subtotal: qty * price }]
      }

      setOrders(prev => [newOrder, ...prev])
      setCartCount(prev => ({ ...prev, [productId]: 0 }))
      setConfirmedMessage(`Commande de ${qty}x ${productName} envoyée à ${shopName} ! Livraison prévue à ${selectedAuditoire.code}.`)
      setTimeout(() => setConfirmedMessage(null), 5000)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Erreur lors de la commande'
      setErrorMessage(msg)
      setTimeout(() => setErrorMessage(null), 4000)
    }
  }

  const handleUpdateOrderStatus = async (orderId: number, status: 'LIVREE_PAYEE' | 'ANNULEE') => {
    try {
      if (currentUser) {
        if (status === 'LIVREE_PAYEE') {
          await api.confirmOrderPayment(orderId)
        } else {
          await api.cancelOrder(orderId, 'CLIENT_ABSENT')
        }
      }
      setOrders(prev => prev.map(o => (o.id === orderId ? { ...o, status } : o)))
    } catch {
      setOrders(prev => prev.map(o => (o.id === orderId ? { ...o, status } : o)))
    }
  }

  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      let user: UserDTO
      if (isRegisterMode) {
        user = await api.register({
          fullName: authFullName,
          email: authEmail,
          password: authPassword,
          defaultAuditoireId: selectedAuditoireId
        })
      } else {
        user = await api.login({
          email: authEmail,
          password: authPassword
        })
      }
      setCurrentUser(user)
      setShowAuthModal(false)
      setAuthPassword('')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Échec de connexion'
      setErrorMessage(msg)
      setTimeout(() => setErrorMessage(null), 4000)
    }
  }

  // Pending orders count per auditoire for the 2D map
  const pendingOrdersCountByAuditoire = orders
    .filter(o => o.status === 'EN_ATTENTE')
    .reduce((acc, order) => {
      acc[order.auditoireId] = (acc[order.auditoireId] || 0) + 1
      return acc
    }, {} as Record<number, number>)

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-16">
      {/* Top Bar */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md border-b border-slate-200">
        <div className="max-w-md mx-auto px-4 py-3 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5">
              <span className="text-xl font-black tracking-tight text-black">Proximo</span>
              <span className="text-xs font-bold uppercase tracking-wider bg-black text-white px-1.5 py-0.5 rounded">UCC</span>
            </div>
            <p className="text-[11px] text-slate-500 font-medium">Campus Limete & Mont-Ngafula</p>
          </div>

          <div className="flex items-center gap-2">
            {currentUser ? (
              <button 
                onClick={() => { api.clearToken(); setCurrentUser(null) }}
                className="text-xs flex items-center gap-1 text-slate-600 hover:text-black bg-slate-100 px-2.5 py-1.5 rounded-full border border-slate-200 font-medium"
                title="Déconnexion"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="truncate max-w-[80px]">{currentUser.fullName.split(' ')[0]}</span>
              </button>
            ) : (
              <button
                onClick={() => setShowAuthModal(true)}
                className="text-xs flex items-center gap-1 bg-black text-white px-3 py-1.5 rounded-full font-semibold shadow-sm active:scale-95"
              >
                <UserIcon className="w-3.5 h-3.5" />
                Connexion
              </button>
            )}
          </div>
        </div>

        {/* Auditoire Selector Bar */}
        <div className="max-w-md mx-auto px-4 py-2 bg-slate-100/80 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
          <div className="flex items-center gap-1.5 truncate max-w-[70%]">
            <MapPin className="w-3.5 h-3.5 text-black shrink-0" />
            <select
              value={selectedAuditoireId}
              onChange={e => setSelectedAuditoireId(Number(e.target.value))}
              className="bg-transparent font-medium text-black focus:outline-none cursor-pointer truncate"
            >
              {auditoires.map(a => (
                <option key={a.id} value={a.id}>
                  {a.code} — {a.name}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-center gap-1 text-[11px] text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200 shrink-0 font-medium">
            <Clock className="w-3 h-3" />
            <span>Ferme à 15h30</span>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="max-w-md mx-auto px-4 pt-2 pb-1 flex border-t border-slate-100 gap-1 text-xs font-semibold">
          <button
            onClick={() => setActiveTab('BUYER')}
            className={`flex-1 py-1.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              activeTab === 'BUYER' ? 'bg-black text-white shadow-sm' : 'text-slate-600 hover:text-black bg-slate-100'
            }`}
          >
            <ShoppingBag className="w-3.5 h-3.5" />
            Acheter
          </button>
          <button
            onClick={() => setActiveTab('VENDOR')}
            className={`flex-1 py-1.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              activeTab === 'VENDOR' ? 'bg-black text-white shadow-sm' : 'text-slate-600 hover:text-black bg-slate-100'
            }`}
          >
            <Store className="w-3.5 h-3.5" />
            Vendre
          </button>
          <button
            onClick={() => setActiveTab('MAP')}
            className={`flex-1 py-1.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              activeTab === 'MAP' ? 'bg-black text-white shadow-sm' : 'text-slate-600 hover:text-black bg-slate-100'
            }`}
          >
            <MapIcon className="w-3.5 h-3.5" />
            Plan 2D
          </button>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-md mx-auto px-4 pt-4">
        {confirmedMessage && (
          <div className="mb-4 p-3 bg-emerald-50 border border-emerald-300 rounded-xl flex items-start gap-2.5 shadow-sm text-emerald-900 text-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-xs text-emerald-800">Commande réussie !</p>
              <p className="text-xs text-emerald-700 mt-0.5">{confirmedMessage}</p>
            </div>
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 p-3 bg-rose-50 border border-rose-300 rounded-xl flex items-start gap-2.5 shadow-sm text-rose-900 text-sm">
            <XCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
            <p className="text-xs text-rose-700">{errorMessage}</p>
          </div>
        )}

        {/* ================= BUYER VIEW ================= */}
        {activeTab === 'BUYER' && (
          <div className="space-y-6">
            <div className="bg-black text-white p-4 rounded-2xl shadow-sm">
              <div className="flex items-center gap-2 mb-1">
                <Sparkles className="w-4 h-4 text-amber-300" />
                <h2 className="text-sm font-bold uppercase tracking-wider text-slate-200">Livraison à votre place</h2>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                Commandez directement auprès des vendeurs présents. Livraison dans votre auditoire (<strong>{selectedAuditoire.code}</strong>) avec paiement en cash à l'arrivée.
              </p>
            </div>

            <div className="space-y-4">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">
                Vendeurs disponibles ({shops.filter(s => s.presentToday).length})
              </h3>

              {shops.map(shop => (
                <div key={shop.id} className="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm space-y-3">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <div>
                      <h4 className="font-bold text-base text-slate-900">{shop.name}</h4>
                      <p className="text-xs text-slate-500">Par {shop.ownerName}</p>
                    </div>
                    <span className="text-[11px] font-semibold bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse"></span>
                      Présent
                    </span>
                  </div>

                  <div className="space-y-2">
                    {shop.products.map(product => {
                      const qty = cartCount[product.id] || 1
                      return (
                        <div key={product.id} className="flex items-center justify-between p-2.5 rounded-xl hover:bg-slate-50 border border-slate-100">
                          <div className="flex items-center gap-3">
                            <span className="text-2xl p-1 bg-slate-100 rounded-lg">{product.photoUrl || '🥪'}</span>
                            <div>
                              <p className="font-semibold text-sm text-slate-800">{product.name}</p>
                              <p className="text-xs font-bold text-black">{product.price.toLocaleString()} FC</p>
                              <p className="text-[10px] text-slate-400">Disponible : {product.availableStock}</p>
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
                              onClick={() => handleOrder(product.id, shop.id, product.name, product.price, shop.name)}
                              disabled={product.availableStock <= 0}
                              className={`text-xs font-semibold px-3 py-1.5 rounded-xl transition-all shadow-sm ${
                                product.availableStock > 0
                                  ? 'bg-black hover:bg-slate-800 text-white active:scale-95'
                                  : 'bg-slate-200 text-slate-400 cursor-not-allowed'
                              }`}
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
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm text-slate-900">Disponibilité du jour</h3>
                <p className="text-xs text-slate-500">Signalez votre présence aux étudiants</p>
              </div>
              <button
                onClick={() => setVendorIsPresent(!vendorIsPresent)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shadow-sm ${
                  vendorIsPresent ? 'bg-emerald-600 text-white' : 'bg-slate-200 text-slate-700'
                }`}
              >
                {vendorIsPresent ? '✓ Je suis là aujourd’hui' : '✕ Déclarer ma présence'}
              </button>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">
                  Commandes à livrer ({orders.filter(o => o.status === 'EN_ATTENTE').length})
                </h3>
                <span className="text-[11px] text-slate-400 font-medium">Groupées par auditoire</span>
              </div>

              {orders.length === 0 ? (
                <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-400 text-xs">
                  Aucune commande enregistrée pour le moment
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
                          <span className="font-bold text-xs text-black">{order.auditoireCode} — {order.auditoireName}</span>
                        </div>
                        <p className="text-xs text-slate-600 mt-0.5">Client : <strong className="text-slate-800">{order.buyerName}</strong></p>
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
                      <div>
                        {order.items.map(it => (
                          <span key={it.id} className="block font-medium text-slate-800">
                            {it.quantity}x {it.productName}
                          </span>
                        ))}
                      </div>
                      <strong className="text-black font-mono text-sm">{order.totalAmount.toLocaleString()} FC (Cash)</strong>
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
                          title="Annuler (client absent)"
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

        {/* ================= MAP 2D VIEW ================= */}
        {activeTab === 'MAP' && (
          <div className="space-y-4">
            <AuditoireMap2D
              auditoires={auditoires}
              highlightAuditoireId={selectedAuditoireId}
              pendingOrdersCountByAuditoire={pendingOrdersCountByAuditoire}
              onSelectAuditoire={aud => setSelectedAuditoireId(aud.id)}
            />
          </div>
        )}
      </main>

      {/* Auth Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-sm w-full p-6 space-y-4 shadow-xl border border-slate-100">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-base text-slate-900">
                {isRegisterMode ? 'Créer un compte UCC' : 'Connexion Proximo UCC'}
              </h3>
              <button 
                onClick={() => setShowAuthModal(false)}
                className="text-slate-400 hover:text-black text-sm"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleAuthSubmit} className="space-y-3">
              {isRegisterMode && (
                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1">Nom complet</label>
                  <input
                    type="text"
                    required
                    value={authFullName}
                    onChange={e => setAuthFullName(e.target.value)}
                    placeholder="Ex: Emmanuel Musasa"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-1 focus:ring-black"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Email UCC / Étudiant</label>
                <input
                  type="email"
                  required
                  value={authEmail}
                  onChange={e => setAuthEmail(e.target.value)}
                  placeholder="etudiant@ucc.cd"
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-1 focus:ring-black"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Mot de passe</label>
                <input
                  type="password"
                  required
                  value={authPassword}
                  onChange={e => setAuthPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-1 focus:ring-black"
                />
              </div>

              <button
                type="submit"
                className="w-full bg-black text-white text-xs font-bold py-2.5 rounded-xl shadow-sm hover:bg-slate-800 transition-all mt-2"
              >
                {isRegisterMode ? 'Créer mon compte' : 'Se connecter'}
              </button>

              <div className="text-center pt-2">
                <button
                  type="button"
                  onClick={() => setIsRegisterMode(!isRegisterMode)}
                  className="text-xs text-slate-500 hover:text-black font-medium"
                >
                  {isRegisterMode ? 'Déjà un compte ? Connectez-vous' : 'Pas de compte ? Inscrivez-vous'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
