package app.controller.dto;

import app.model.EstadoSolicitud;

public class CambiarEstadoVisitaRequest {

    private EstadoSolicitud estado;

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
}