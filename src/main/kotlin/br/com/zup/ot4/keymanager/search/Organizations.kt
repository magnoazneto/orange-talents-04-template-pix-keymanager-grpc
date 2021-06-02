package br.com.zup.ot4.keymanager.search

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files.*
import java.nio.file.Paths

class Organizations() {
    companion object {
        private var names = mutableMapOf<String, String>()
        init {
            val bufferedReader = newBufferedReader(Paths.get(".\\ParticipantesSTRport.csv"))
            val csvParser = CSVParser(bufferedReader, CSVFormat.DEFAULT)
            csvParser.forEach { record ->
                names[record.get(0)] = record.get(1).trim()
            }
        }
        fun name(participant: String): String {
           return names[participant]!!
        }
    }






}
