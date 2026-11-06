package utnfc.isi.back.taller.ordenes.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;

class EstadoOrdenTest {

    @Test
    void transicionesDelCaminoPrincipal() {
        assertTrue(EstadoOrden.RECIBIDA.puedePasarA(EstadoOrden.EN_DIAGNOSTICO));
        assertTrue(EstadoOrden.PRESUPUESTADA.puedePasarA(EstadoOrden.ACEPTADA));
        assertTrue(EstadoOrden.EN_REPARACION.puedePasarA(EstadoOrden.APROBACION_ADICIONAL));
        assertTrue(EstadoOrden.LISTA_PARA_RETIRAR.puedePasarA(EstadoOrden.ENTREGADA));
    }

    @Test
    void noSeReparaSinPresupuestoAceptado() {
        assertFalse(EstadoOrden.EN_DIAGNOSTICO.puedePasarA(EstadoOrden.EN_REPARACION));
        assertFalse(EstadoOrden.PRESUPUESTADA.puedePasarA(EstadoOrden.EN_REPARACION));
    }

    @Test
    void cambiarEstadoRechazaTransicionInvalida() {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setEstado(EstadoOrden.RECIBIDA);
        ServiceException ex = assertThrows(ServiceException.class, () -> orden.cambiarEstado(EstadoOrden.ENTREGADA));
        assertEquals(409, ex.getStatus());
    }
}
