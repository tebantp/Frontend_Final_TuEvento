package com.tuevento.tueventofinal.data.remote

import com.tuevento.tueventofinal.data.model.*
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────────────────────────────────────
// CORRECCIONES APLICADAS:
//  #4  — Se eliminan 9 rutas inexistentes en el backend:
//         GET /api/inscripciones, GET /api/asistencias, GET /api/qr,
//         GET /api/qr/codigo/{codigo}, POST /api/asistencias,
//         PATCH /api/usuarios/{id}/rol, PATCH /api/usuarios/{id}/estado,
//         PATCH /api/eventos/{id}/estado, GET /api/estadisticas/*
//  #4  — Se corrigen las rutas de PATCH publicar/cancelar evento
//  #3  — Se añade POST /api/asistencias/validar-qr (endpoint real de QR)
//  #3  — Se corrige POST /api/asistencias/marcar (ruta real del backend)
//  #5  — Se elimina uploadFotoPerfil (endpoint inexistente en el backend)
//  #8  — DELETE inscripcion cambiado a PATCH /cancelar
//  #10 — PATCH notificaciones/{id}/leida → /leer
//  #11 — Se añade GET /notificaciones/usuario/{id}/no-leidas
//  #12 — Se añade PATCH /chat/{id} para editar mensajes
//  #13 — Se elimina POST /api/qr y CodigoQRRequest (QR se genera con inscripcion)
//  #13 — GET /api/qr/codigo/{codigo} eliminado; se usa /api/qr/{codigoUnico}
// ─────────────────────────────────────────────────────────────────────────────

interface ApiService {

    // ─── USUARIOS (5 endpoints — rutas 100% verificadas) ─────────────────────
    @GET("api/usuarios")
    suspend fun getUsuarios(): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Long): Response<UsuarioResponse>

    @POST("api/usuarios")
    suspend fun createUsuario(@Body request: UsuarioRequest): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Long,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long): Response<Void>

    // ─── EVENTOS (7 endpoints) ────────────────────────────────────────────────
    @GET("api/eventos")
    suspend fun getEventos(): List<EventoResponse>

    @GET("api/eventos/{id}")
    suspend fun getEventoById(@Path("id") id: Long): Response<EventoResponse>

    @GET("api/eventos/organizador/{id}")
    suspend fun getEventosByOrganizador(@Path("id") id: Long): List<EventoResponse>

    @POST("api/eventos")
    suspend fun createEvento(
        @Query("organizadorId") organizadorId: Long,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    @PUT("api/eventos/{id}")
    suspend fun updateEvento(
        @Path("id") id: Long,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    // CORRECCIÓN #4: Las rutas de publicar/cancelar son PATCH con sufijo
    // correcto. El backend NO tiene PATCH /eventos/{id}/estado como query param.
    @PATCH("api/eventos/{id}/publicar")
    suspend fun publicarEvento(@Path("id") id: Long): Response<EventoResponse>

    @PATCH("api/eventos/{id}/cancelar")
    suspend fun cancelarEvento(@Path("id") id: Long): Response<EventoResponse>

    @DELETE("api/eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: Long): Response<Void>

    // ─── INSCRIPCIONES (5 endpoints) ─────────────────────────────────────────
    // CORRECCIÓN #4: GET /api/inscripciones (lista global) NO existe en el backend.
    @GET("api/inscripciones/{id}")
    suspend fun getInscripcionById(@Path("id") id: Long): Response<InscripcionResponse>

    @GET("api/inscripciones/usuario/{id}")
    suspend fun getInscripcionesByUsuario(@Path("id") id: Long): List<InscripcionResponse>

    @GET("api/inscripciones/evento/{id}")
    suspend fun getInscripcionesByEvento(@Path("id") id: Long): List<InscripcionResponse>

    @POST("api/inscripciones")
    suspend fun createInscripcion(@Body request: InscripcionRequest): Response<InscripcionResponse>

    // CORRECCIÓN #8: Era @DELETE que borraba el registro permanentemente.
    // El backend espera PATCH /cancelar para cambiar estado a CANCELADA.
    @PATCH("api/inscripciones/{id}/cancelar")
    suspend fun cancelarInscripcion(@Path("id") id: Long): Response<Void>

    // ─── ASISTENCIAS (3 endpoints) ────────────────────────────────────────────
    // CORRECCIÓN #3 y #4:
    //   - POST /api/asistencias NO existe. La ruta real es /api/asistencias/marcar
    //   - GET /api/asistencias (lista global) NO existe en el backend
    //   - Se añade el endpoint de validación por QR que hace ambos pasos en uno
    @POST("api/asistencias/marcar")
    suspend fun marcarAsistencia(@Body request: AsistenciaRequest): Response<AsistenciaResponse>

    // Endpoint correcto para validar QR y registrar asistencia en un solo paso
    @POST("api/asistencias/validar-qr")
    suspend fun validarQR(
        @Query("codigoQR") codigoQR: String,
        @Query("staffId") staffId: Long
    ): Response<AsistenciaResponse>

    @GET("api/asistencias/evento/{id}")
    suspend fun getAsistenciasByEvento(@Path("id") id: Long): List<AsistenciaResponse>

    @GET("api/asistencias/evento/{id}/count")
    suspend fun countAsistenciasByEvento(@Path("id") id: Long): Response<Map<String, Long>>

    // ─── CODIGOS QR (2 endpoints — se eliminaron los 2 inexistentes) ──────────
    // CORRECCIÓN #13: POST /api/qr y GET /api/qr (lista global) no existen.
    // El QR se genera automáticamente con la inscripción.
    // GET /api/qr/codigo/{codigo} tampoco existe — la ruta real es /{codigoUnico}
    @GET("api/qr/{codigoUnico}")
    suspend fun getQRByCodigo(@Path("codigoUnico") codigoUnico: String): Response<CodigoQRResponse>

    @GET("api/qr/inscripcion/{id}")
    suspend fun getQRByInscripcion(@Path("id") id: Long): Response<CodigoQRResponse>

    // ─── FEEDBACK (2 endpoints) ───────────────────────────────────────────────
    @GET("api/feedback/evento/{id}")
    suspend fun getFeedbackByEvento(@Path("id") id: Long): List<FeedbackResponse>

    @POST("api/feedback")
    suspend fun createFeedback(@Body request: FeedbackRequest): Response<FeedbackResponse>

    // ─── IMAGENES (3 endpoints) ───────────────────────────────────────────────
    @GET("api/imagenes/evento/{id}")
    suspend fun getImagenesByEvento(@Path("id") id: Long): List<ImagenEventoResponse>

    @POST("api/imagenes")
    suspend fun addImagenEvento(@Body request: ImagenEventoRequest): Response<ImagenEventoResponse>

    @DELETE("api/imagenes/{id}")
    suspend fun deleteImagenEvento(@Path("id") id: Long): Response<Void>

    // ─── CHAT (4 endpoints) ───────────────────────────────────────────────────
    @GET("api/chat/evento/{id}")
    suspend fun getMensajesByEvento(@Path("id") id: Long): List<MensajeChatResponse>

    @POST("api/chat")
    suspend fun enviarMensaje(@Body request: MensajeChatRequest): Response<MensajeChatResponse>

    // CORRECCIÓN #12: Se añade endpoint faltante para editar mensajes
    @PATCH("api/chat/{id}")
    suspend fun editarMensaje(
        @Path("id") id: Long,
        @Query("contenido") contenido: String
    ): Response<MensajeChatResponse>

    @DELETE("api/chat/{id}")
    suspend fun eliminarMensaje(@Path("id") id: Long): Response<Void>

    // ─── NOTIFICACIONES (5 endpoints) ────────────────────────────────────────
    @POST("api/notificaciones")
    suspend fun createNotificacion(@Body request: NotificacionRequest): Response<NotificacionResponse>

    @GET("api/notificaciones/usuario/{id}")
    suspend fun getNotificacionesByUsuario(@Path("id") id: Long): List<NotificacionResponse>

    // CORRECCIÓN #11: Endpoint faltante para el badge de no-leídas
    @GET("api/notificaciones/usuario/{id}/no-leidas")
    suspend fun getNotificacionesNoLeidas(@Path("id") id: Long): List<NotificacionResponse>

    // CORRECCIÓN #10: Era /leida → debe ser /leer (ruta real del backend)
    @PATCH("api/notificaciones/{id}/leer")
    suspend fun marcarLeida(@Path("id") id: Long): Response<NotificacionResponse>

    @DELETE("api/notificaciones/{id}")
    suspend fun deleteNotificacion(@Path("id") id: Long): Response<Void>
}
