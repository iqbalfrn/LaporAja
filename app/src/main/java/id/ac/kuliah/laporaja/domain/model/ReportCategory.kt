package id.ac.kuliah.laporaja.domain.model

enum class ReportCategory(val value: String, val label: String) {
    SAMPAH("sampah", "Sampah menumpuk"),
    SALURAN("saluran", "Saluran air mampet"),
    JALAN("jalan", "Jalan rusak"),
    PENERANGAN("penerangan", "Lampu jalan mati"),
    FASUM("fasum", "Fasilitas umum kotor"),
    LAINNYA("lainnya", "Lainnya"),
    ;

    companion object {
        fun from(raw: String?): ReportCategory = entries.firstOrNull { it.value == raw } ?: LAINNYA
    }
}
