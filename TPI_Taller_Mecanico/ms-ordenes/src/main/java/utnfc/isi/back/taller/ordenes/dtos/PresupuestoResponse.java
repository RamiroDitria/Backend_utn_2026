package utnfc.isi.back.taller.ordenes.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import utnfc.isi.back.taller.ordenes.models.Presupuesto;
import utnfc.isi.back.taller.ordenes.models.enums.DecisionPresupuesto;
import utnfc.isi.back.taller.ordenes.models.enums.TipoPresupuesto;

public record PresupuestoResponse(
        Long id, TipoPresupuesto tipo, BigDecimal montoRepuestos, BigDecimal montoManoObra,
        BigDecimal otrosCargos, BigDecimal total, LocalDate fechaEmision, LocalDate fechaVencimiento,
        DecisionPresupuesto decision, String canalDecision, String registradaPor, LocalDateTime fechaDecision) {

    public static PresupuestoResponse from(Presupuesto p) {
        return new PresupuestoResponse(p.getId(), p.getTipo(), p.getMontoRepuestos(), p.getMontoManoObra(),
                p.getOtrosCargos(), p.getTotal(), p.getFechaEmision(), p.getFechaVencimiento(),
                p.getDecision(), p.getCanalDecision(), p.getRegistradaPor(), p.getFechaDecision());
    }
}
