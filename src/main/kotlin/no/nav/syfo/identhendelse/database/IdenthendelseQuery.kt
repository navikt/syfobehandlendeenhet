package no.nav.syfo.identhendelse.database

import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.nowUTC

const val queryUpdatePerson =
    """
        UPDATE PERSON
        SET personident = ?, updated_at = ?
        WHERE personident = ?
    """

fun DatabaseInterface.updatePerson(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int {
    var updatedRows: Int
    val now = nowUTC()
    this.connection.use { connection ->
        updatedRows = connection.prepareStatement(queryUpdatePerson).use {
            it.setString(1, nyPersonident.value)
            it.setObject(2, now)
            it.setString(3, oldIdent.value)
            it.executeUpdate()
        }.also {
            connection.commit()
        }
    }
    return updatedRows
}

const val queryDeletePerson =
    """
        DELETE FROM PERSON
        WHERE personident = ?
    """

fun DatabaseInterface.deletePerson(personIdent: PersonIdentNumber): Int {
    var deletedRows: Int
    this.connection.use { connection ->
        deletedRows = connection.prepareStatement(queryDeletePerson).use {
            it.setString(1, personIdent.value)
            it.executeUpdate()
        }.also {
            connection.commit()
        }
    }
    return deletedRows
}
