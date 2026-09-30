fun main() {
val skills = mutableSetOf("Kotlin", "Java")
skills.add("Python")
skills.add("Kotlin")
println("jumlah Skills = ${skills.size}")
println("Apakah Swift ada di dalam Set? ${"Swift" in skills}") 
println("Apakah Kotlin ada di dalam Set? ${"Kotlin" in skills}") 
} 

// Ukuran pada set tidak bertambah karena set berguna untuk menyimpan dan tidak boleh ada duplikasi.
// karena pada kasus/latihan ini "Kotlin" sudah di tulis awal mutablesetof jadi penambahan skills "Kotlin" yang baru diabaikan oleh sistemfun main()