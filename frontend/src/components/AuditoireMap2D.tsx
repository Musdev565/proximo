import React, { useState } from 'react'
import { MapPin, Navigation, CornerDownRight, Layers } from 'lucide-react'
import type { AuditoireDTO } from '../api/client'

interface AuditoireMap2DProps {
  auditoires: AuditoireDTO[]
  highlightAuditoireId?: number | null
  pendingOrdersCountByAuditoire?: Record<number, number>
  onSelectAuditoire?: (auditoire: AuditoireDTO) => void
}

export const AuditoireMap2D: React.FC<AuditoireMap2DProps> = ({
  auditoires,
  highlightAuditoireId,
  pendingOrdersCountByAuditoire = {},
  onSelectAuditoire
}) => {
  const [selectedLevel, setSelectedLevel] = useState<number>(1)

  const levelAuditoires = auditoires
    .filter(a => a.level === selectedLevel)
    .sort((a, b) => (a.positionOrder || 0) - (b.positionOrder || 0))

  const highlightedAuditoire = auditoires.find(a => a.id === highlightAuditoireId)

  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm space-y-4">
      {/* Level Selector */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-xs font-bold text-slate-800 uppercase tracking-wider">
          <Layers className="w-4 h-4 text-black" />
          <span>Plan 2D Campus UCC</span>
        </div>

        <div className="flex bg-slate-100 p-1 rounded-xl text-xs font-semibold border border-slate-200">
          {[1, 2, 3].map(lvl => (
            <button
              key={lvl}
              onClick={() => setSelectedLevel(lvl)}
              className={`px-3 py-1 rounded-lg transition-all ${
                selectedLevel === lvl
                  ? 'bg-black text-white shadow-sm'
                  : 'text-slate-600 hover:text-black'
              }`}
            >
              Niveau {lvl}
            </button>
          ))}
        </div>
      </div>

      {/* Corridor Visual Container */}
      <div className="bg-slate-50 border border-slate-200 rounded-xl p-3 space-y-2">
        {/* Entrance Marker */}
        <div className="flex items-center justify-between text-[11px] text-slate-500 font-medium px-2 py-1 bg-white rounded-lg border border-slate-200">
          <span className="flex items-center gap-1">
            <Navigation className="w-3.5 h-3.5 text-black" />
            Entrée & Escaliers Principaux (Niveau {selectedLevel})
          </span>
          <span className="text-[10px] text-slate-400">Point de départ</span>
        </div>

        {/* Vertical corridor list */}
        <div className="space-y-2 pt-1">
          {levelAuditoires.length === 0 ? (
            <div className="text-center py-6 text-xs text-slate-400">
              Aucun auditoire configuré pour ce niveau
            </div>
          ) : (
            levelAuditoires.map((aud, index) => {
              const isTarget = aud.id === highlightAuditoireId
              const orderCount = pendingOrdersCountByAuditoire[aud.id] || 0

              return (
                <div
                  key={aud.id}
                  onClick={() => onSelectAuditoire && onSelectAuditoire(aud)}
                  className={`p-3 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
                    isTarget
                      ? 'bg-amber-50 border-amber-400 ring-2 ring-amber-200 shadow-sm'
                      : orderCount > 0
                      ? 'bg-white border-black/40 hover:border-black'
                      : 'bg-white border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs shrink-0 ${
                        isTarget
                          ? 'bg-black text-white'
                          : orderCount > 0
                          ? 'bg-slate-900 text-white'
                          : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {index + 1}
                    </div>

                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-xs text-slate-900">{aud.code}</span>
                        <span className="text-xs text-slate-600 font-medium">— {aud.name}</span>
                      </div>
                      <p className="text-[11px] text-slate-500">
                        {aud.promotion || 'Toutes promotions'} • Côté {aud.side || 'couloir'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {orderCount > 0 && (
                      <span className="bg-black text-white text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center gap-1">
                        <MapPin className="w-2.5 h-2.5" />
                        {orderCount} cmd
                      </span>
                    )}

                    {isTarget && (
                      <span className="text-[10px] font-bold uppercase tracking-wider text-amber-800 bg-amber-100 px-2 py-0.5 rounded-md">
                        Cible
                      </span>
                    )}
                  </div>
                </div>
              )
            })
          )}
        </div>
      </div>

      {/* Explanatory text according to 06-carte-auditoires.md */}
      {highlightedAuditoire && (
        <div className="p-3 bg-amber-50/80 border border-amber-200 rounded-xl text-xs text-amber-950 flex items-start gap-2">
          <CornerDownRight className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <p>
            <strong>Itinéraire de livraison :</strong> Niveau {highlightedAuditoire.level},{' '}
            {highlightedAuditoire.positionOrder || 1}
            {highlightedAuditoire.positionOrder === 1 ? 'er' : 'e'} auditoire depuis l'escalier (
            {highlightedAuditoire.code} — {highlightedAuditoire.name}, côté{' '}
            {highlightedAuditoire.side || 'gauche'}).
          </p>
        </div>
      )}
    </div>
  )
}
