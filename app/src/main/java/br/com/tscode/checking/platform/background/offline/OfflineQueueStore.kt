package br.com.tscode.checking.platform.background.offline

/**
 * Storage backend for the offline check queue's JSON blob. Abstracted behind an interface so the queue
 * stays a plain-JVM unit (a fake in-memory store in tests) while production encrypts the precise GPS
 * coordinates at rest (LGPD art. 46). All calls happen under the queue's own Mutex, so implementations
 * do not need to be internally synchronized.
 */
interface OfflineQueueStore {
    suspend fun read(): String
    suspend fun write(json: String)
}
