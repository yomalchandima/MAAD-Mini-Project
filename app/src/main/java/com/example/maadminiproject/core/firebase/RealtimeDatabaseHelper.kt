package com.example.maadminiproject.core.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * A generic helper class for performing Firebase Realtime Database operations.
 *
 * This class provides standard CRUD and observation methods using callbacks.
 * It is designed to be domain-agnostic and relies on [FirebaseManager] for
 * database instance management.
 */
@Suppress("unused")
class RealtimeDatabaseHelper {

    /**
     * Writes a value to the specified database reference.
     *
     * @param reference The [DatabaseReference] where the data will be written.
     * @param value The data object to write.
     * @param onSuccess Optional callback invoked when the write operation succeeds.
     * @param onFailure Optional callback invoked with an [Exception] if the write operation fails.
     */
    fun writeData(
        reference: DatabaseReference,
        value: Any,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        reference.setValue(value).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess?.invoke()
            } else {
                task.exception?.let { onFailure?.invoke(it) }
            }
        }
    }

    /**
     * Performs a partial update on the specified database reference.
     *
     * @param reference The [DatabaseReference] to update.
     * @param updates A map of paths (relative to the reference) to new values.
     * @param onSuccess Optional callback invoked when the update operation succeeds.
     * @param onFailure Optional callback invoked with an [Exception] if the update operation fails.
     */
    fun updateData(
        reference: DatabaseReference,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        reference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess?.invoke()
            } else {
                task.exception?.let { onFailure?.invoke(it) }
            }
        }
    }

    /**
     * Deletes the data at the specified database reference.
     *
     * @param reference The [DatabaseReference] to delete.
     * @param onSuccess Optional callback invoked when the deletion succeeds.
     * @param onFailure Optional callback invoked with an [Exception] if the deletion fails.
     */
    fun deleteData(
        reference: DatabaseReference,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        reference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess?.invoke()
            } else {
                task.exception?.let { onFailure?.invoke(it) }
            }
        }
    }

    /**
     * Reads data from the specified reference once.
     *
     * @param reference The [DatabaseReference] to read from.
     * @param onData Callback invoked with the [DataSnapshot] upon success.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     */
    fun readOnce(
        reference: DatabaseReference,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit
    ) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error)
            }
        })
    }

    /**
     * Starts observing data changes at the specified reference.
     *
     * @param reference The [DatabaseReference] to observe.
     * @param onData Callback invoked with the [DataSnapshot] whenever data changes.
     * @param onFailure Callback invoked with a [DatabaseError] if the observation is cancelled.
     * @return The [ValueEventListener] created for this observation, which can be used to stop it later.
     */
    fun observe(
        reference: DatabaseReference,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error)
            }
        }
        reference.addValueEventListener(listener)
        return listener
    }

    /**
     * Removes a previously registered [ValueEventListener] from the specified reference.
     *
     * @param reference The [DatabaseReference] the listener is attached to.
     * @param listener The [ValueEventListener] to remove.
     */
    fun removeListener(
        reference: DatabaseReference,
        listener: ValueEventListener
    ) {
        reference.removeEventListener(listener)
    }
}
