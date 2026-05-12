package com.tuevento.tueventofinal.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuevento.tueventofinal.data.model.EstadisticasOrganizador
import com.tuevento.tueventofinal.data.remote.AsistenciaRepository
import com.tuevento.tueventofinal.data.remote.EventoRepository
import com.tuevento.tueventofinal.data.remote.InscripcionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportesState {
    object Idle : ReportesState()
    object Loading : ReportesState()
    data class Success(val stats: EstadisticasOrganizador) : ReportesState()
    data class Error(val message: String) : ReportesState()
}

// CORRECCIÓN #7: Se elimina EstadisticasRepository — los endpoints /estadisticas/*
// no existen en el backend. Las estadísticas se calculan en el cliente combinando:
//   - getEventosByOrganizador()      → GET /api/eventos/organizador/{id}
//   - getInscripcionesByEvento()     → GET /api/inscripciones/evento/{id}
//   - getAsistenciasByEvento()       → GET /api/asistencias/evento/{id}
class ReportesViewModel(
    private val eventoRepo: EventoRepository = EventoRepository(),
    private val inscripcionRepo: InscripcionRepository = InscripcionRepository(),
    private val asistenciaRepo: AsistenciaRepository = AsistenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesState>(ReportesState.Idle)
    val uiState: StateFlow<ReportesState> = _uiState

    fun loadEstadisticas(organizadorId: Long) {
        viewModelScope.launch {
            _uiState.value = ReportesState.Loading
            try {
                val eventos = eventoRepo.getEventosByOrganizador(organizadorId)

                var totalInscritos = 0
                var totalAsistentes = 0

                // Consultar inscripciones y asistencias para cada evento en paralelo
                val jobs = eventos.map { evento ->
                    async {
                        val inscripciones = runCatching {
                            inscripcionRepo.getInscripcionesByEvento(evento.id)
                        }.getOrDefault(emptyList())

                        val asistencias = runCatching {
                            asistenciaRepo.getAsistenciasByEvento(evento.id)
                        }.getOrDefault(emptyList())

                        Pair(inscripciones.size, asistencias.count { it.presente })
                    }
                }

                jobs.forEach { deferred ->
                    val (inscritos, asistentes) = deferred.await()
                    totalInscritos += inscritos
                    totalAsistentes += asistentes
                }

                val eventosPorEstado = eventos
                    .groupBy { it.estado.name }
                    .mapValues { it.value.size }

                val tasaAsistencia = if (totalInscritos > 0) {
                    (totalAsistentes.toFloat() / totalInscritos.toFloat()) * 100f
                } else 0f

                val stats = EstadisticasOrganizador(
                    totalEventos = eventos.size,
                    totalInscritos = totalInscritos,
                    totalAsistentes = totalAsistentes,
                    tasaAsistencia = tasaAsistencia,
                    eventosPorEstado = eventosPorEstado
                )

                _uiState.value = ReportesState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = ReportesState.Error("Error al generar reporte: ${e.message}")
            }
        }
    }

    fun generarCSV(stats: EstadisticasOrganizador): String {
        val sb = StringBuilder()
        sb.appendLine("Métrica,Valor")
        sb.appendLine("Total eventos,${stats.totalEventos}")
        sb.appendLine("Total inscritos,${stats.totalInscritos}")
        sb.appendLine("Total asistentes,${stats.totalAsistentes}")
        sb.appendLine("Tasa de asistencia,${String.format("%.1f", stats.tasaAsistencia)}%")
        stats.eventosPorEstado.forEach { (estado, cantidad) ->
            sb.appendLine("Eventos $estado,$cantidad")
        }
        return sb.toString()
    }
}
