package com.tuevento.tueventofinal.data.remote

import com.tuevento.tueventofinal.data.model.*

// ─────────────────────────────────────────────────────────────────────────────
// CORRECCIONES APLICADAS:
//  #8  — InscripcionRepository: deleteInscripcion → cancelarInscripcion (PATCH)
//  #3  — AsistenciaRepository: registrarAsistencia → marcarAsistencia; se añade validarQR()
//  #13 — QRRepository: se eliminan generarQR() y getCodigosQR() (endpoints inexistentes)
//  #4  — EventoRepository: moderarEvento() eliminado (ruta no existe); se añaden publicar/cancelar
//  #4  — Se elimina EstadisticasRepository (los endpoints /estadisticas/* no existen)
//  #11 — NotificacionRepository: se añade getNotificacionesNoLeidas()
//  #12 — ChatRepository: se añade editarMensaje()
// ─────────────────────────────────────────────────────────────────────────────

class UsuarioRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getUsuarios() = api.getUsuarios()
    suspend fun getUsuarioById(id: Long) = api.getUsuarioById(id)
    suspend fun createUsuario(request: UsuarioRequest) = api.createUsuario(request)
    suspend fun updateUsuario(id: Long, request: UsuarioRequest) = api.updateUsuario(id, request)
    suspend fun deleteUsuario(id: Long) = api.deleteUsuario(id)
}

class EventoRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getEventos() = api.getEventos()
    suspend fun getEventoById(id: Long) = api.getEventoById(id)
    suspend fun getEventosByOrganizador(id: Long) = api.getEventosByOrganizador(id)
    suspend fun createEvento(organizadorId: Long, request: EventoRequest) =
        api.createEvento(organizadorId, request)
    suspend fun updateEvento(id: Long, request: EventoRequest) = api.updateEvento(id, request)
    suspend fun deleteEvento(id: Long) = api.deleteEvento(id)

    // CORRECCIÓN #4: El backend expone dos rutas PATCH específicas, no una genérica
    suspend fun publicarEvento(id: Long) = api.publicarEvento(id)
    suspend fun cancelarEvento(id: Long) = api.cancelarEvento(id)
}

class InscripcionRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getInscripcionById(id: Long) = api.getInscripcionById(id)
    suspend fun getInscripcionesByUsuario(id: Long) = api.getInscripcionesByUsuario(id)
    suspend fun getInscripcionesByEvento(id: Long) = api.getInscripcionesByEvento(id)
    suspend fun createInscripcion(request: InscripcionRequest) = api.createInscripcion(request)

    // CORRECCIÓN #8: Antes llamaba a DELETE que borraba el registro permanentemente.
    // Ahora llama a PATCH /cancelar que cambia el estado a CANCELADA sin perder datos.
    suspend fun cancelarInscripcion(id: Long) = api.cancelarInscripcion(id)
}

class AsistenciaRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getAsistenciasByEvento(id: Long) = api.getAsistenciasByEvento(id)
    suspend fun countAsistenciasByEvento(id: Long) = api.countAsistenciasByEvento(id)

    // CORRECCIÓN #3: Ruta real del backend para marcar asistencia manual
    suspend fun marcarAsistencia(request: AsistenciaRequest) = api.marcarAsistencia(request)

    // CORRECCIÓN #3: Endpoint único que valida el QR y registra asistencia en un solo paso.
    // Antes el VM hacía 2 llamadas con rutas incorrectas. Ahora usa este endpoint dedicado.
    suspend fun validarQR(codigoQR: String, staffId: Long) = api.validarQR(codigoQR, staffId)
}

class QRRepository(private val api: ApiService = NetworkModule.apiService) {
    // CORRECCIÓN #13: Se eliminaron generarQR() y getCodigosQR() (no existen en el backend).
    // El QR se genera automáticamente al crear una inscripción (InscripcionResponse.urlQR).
    suspend fun getQRByInscripcion(id: Long) = api.getQRByInscripcion(id)
    suspend fun getQRByCodigo(codigoUnico: String) = api.getQRByCodigo(codigoUnico)
}

class FeedbackRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getFeedbackByEvento(id: Long) = api.getFeedbackByEvento(id)
    suspend fun createFeedback(request: FeedbackRequest) = api.createFeedback(request)
}

class ImagenRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getImagenesByEvento(id: Long) = api.getImagenesByEvento(id)
    suspend fun addImagenEvento(request: ImagenEventoRequest) = api.addImagenEvento(request)
    suspend fun deleteImagenEvento(id: Long) = api.deleteImagenEvento(id)
}

class ChatRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getMensajesByEvento(id: Long) = api.getMensajesByEvento(id)
    suspend fun enviarMensaje(request: MensajeChatRequest) = api.enviarMensaje(request)

    // CORRECCIÓN #12: Endpoint faltante para editar mensajes
    suspend fun editarMensaje(id: Long, contenido: String) = api.editarMensaje(id, contenido)
    suspend fun eliminarMensaje(id: Long) = api.eliminarMensaje(id)
}

class NotificacionRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun getNotificacionesByUsuario(id: Long) = api.getNotificacionesByUsuario(id)

    // CORRECCIÓN #11: Endpoint faltante para mostrar badge de no-leídas
    suspend fun getNotificacionesNoLeidas(id: Long) = api.getNotificacionesNoLeidas(id)

    // CORRECCIÓN #10: Ruta /leer (no /leida como estaba antes)
    suspend fun marcarLeida(id: Long) = api.marcarLeida(id)
    suspend fun deleteNotificacion(id: Long) = api.deleteNotificacion(id)
    suspend fun createNotificacion(request: NotificacionRequest) = api.createNotificacion(request)
}

// CORRECCIÓN #7 y #13: EstadisticasRepository ELIMINADO completamente.
// Los endpoints GET /api/estadisticas/* no existen en el backend.
// Las estadísticas se calculan en el cliente combinando llamadas reales.
// Ver AdminViewModel y OrganizadorViewModel para la implementación correcta.
