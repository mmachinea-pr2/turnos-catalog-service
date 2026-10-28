package ar.edu.programacion2.catalog.catalog.application.port.out;

/** Error de consulta seguro para registrar: no contiene cuerpos ni credenciales. */
public class SnapshotFetchException extends RuntimeException {
    public SnapshotFetchException(String message) {
        super(message);
    }
}
