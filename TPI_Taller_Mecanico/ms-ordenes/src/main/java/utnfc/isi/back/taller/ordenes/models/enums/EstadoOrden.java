package utnfc.isi.back.taller.ordenes.models.enums;

/** Estados de la orden de trabajo y transiciones permitidas (diagrama de estados). */
public enum EstadoOrden {
    RECIBIDA,
    EN_DIAGNOSTICO,
    PRESUPUESTADA,
    ACEPTADA,
    RECHAZADA,
    ESPERANDO_REPUESTOS,
    EN_REPARACION,
    APROBACION_ADICIONAL,
    LISTA_PARA_RETIRAR,
    ENTREGADA;

    public boolean puedePasarA(EstadoOrden destino) {
        return switch (this) {
            case RECIBIDA -> destino == EN_DIAGNOSTICO;
            case EN_DIAGNOSTICO -> destino == PRESUPUESTADA;
            case PRESUPUESTADA -> destino == ACEPTADA || destino == RECHAZADA;
            case ACEPTADA -> destino == ESPERANDO_REPUESTOS || destino == EN_REPARACION;
            case ESPERANDO_REPUESTOS -> destino == EN_REPARACION;
            case EN_REPARACION -> destino == APROBACION_ADICIONAL || destino == LISTA_PARA_RETIRAR;
            case APROBACION_ADICIONAL -> destino == EN_REPARACION || destino == ESPERANDO_REPUESTOS;
            case RECHAZADA -> destino == LISTA_PARA_RETIRAR;
            case LISTA_PARA_RETIRAR -> destino == ENTREGADA;
            case ENTREGADA -> false;
        };
    }
}
