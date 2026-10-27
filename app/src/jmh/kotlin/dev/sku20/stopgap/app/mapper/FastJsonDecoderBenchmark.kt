package dev.sku20.stopgap.app.mapper

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class SampleString(val data: String)
data class SampleWrapper<T>(val data: T)
data class SampleList(val items: List<String>)
data class SampleMap(val entries: Map<String, String>)
data class SampleMapAny(val entries: Map<String, Any>)

// Results (thrpt, ops/ms, 2 forks x 10 iterations x 1s):
//   decodeKType           72600  ± 800
//   decodeList            54137  ± 105
//   decodeMap             39217  ± 336
//   decodeSampleMapAny    38371  ± 245
//   decodeKTypeInline     16672  ± 86
//   decodeGeneric         11080  ± 920
//   decodeMapAny           9676  ± 58
//
// Findings:
// - Matches FastjsonDeserializeBenchmark: the decoder's null check and try/catch cost nothing measurable.
// - SampleMapAny (~38k) is as fast as SampleMap, not ~5.7k: the old benchmark decoded SampleWrapper<Map<String, Any>>
//   by mistake, so its "penalties stack" finding measured the generic wrapper, not an Any-valued field.
// - A top-level Map<String, Any> (~9.7k) is ~4x slower than the same map as a field. Unverified: the top-level
//   ParameterizedType may resolve to a slower generic reader than the field's.
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
open class FastJsonDecoderBenchmark {

    private val decoder = FastJsonDecoder()

    private lateinit var sampleStringBytes: ByteArray
    private lateinit var sampleListBytes: ByteArray
    private lateinit var sampleMapBytes: ByteArray
    private lateinit var rawMapBytes: ByteArray
    private lateinit var sampleStringKType: KType
    private lateinit var sampleWrapperKType: KType
    private lateinit var sampleListKType: KType
    private lateinit var sampleMapKType: KType
    private lateinit var sampleMapAnyKType: KType
    private lateinit var mapAnyKType: KType

    @Setup
    fun setup() {
        sampleStringBytes = """{"data":"alice"}""".toByteArray()
        sampleListBytes = """{"items":["alice"]}""".toByteArray()
        sampleMapBytes = """{"entries":{"key":"alice"}}""".toByteArray()
        rawMapBytes = """{"key":"alice"}""".toByteArray()
        sampleStringKType = typeOf<SampleString>()
        sampleWrapperKType = typeOf<SampleWrapper<String>>()
        sampleListKType = typeOf<SampleList>()
        sampleMapKType = typeOf<SampleMap>()
        sampleMapAnyKType = typeOf<SampleMapAny>()
        mapAnyKType = typeOf<Map<String, Any>>()
    }

    @Benchmark
    fun decodeKType(bh: Blackhole) {
        bh.consume(decoder.decode<SampleString>(sampleStringBytes, sampleStringKType))
    }

    @Benchmark
    fun decodeKTypeInline(bh: Blackhole) {
        bh.consume(decoder.decode<SampleString>(sampleStringBytes, typeOf<SampleString>()))
    }

    @Benchmark
    fun decodeGeneric(bh: Blackhole) {
        bh.consume(decoder.decode<SampleWrapper<String>>(sampleStringBytes, sampleWrapperKType))
    }

    @Benchmark
    fun decodeList(bh: Blackhole) {
        bh.consume(decoder.decode<SampleList>(sampleListBytes, sampleListKType))
    }

    @Benchmark
    fun decodeMap(bh: Blackhole) {
        bh.consume(decoder.decode<SampleMap>(sampleMapBytes, sampleMapKType))
    }

    @Benchmark
    fun decodeSampleMapAny(bh: Blackhole) {
        bh.consume(decoder.decode<SampleMapAny>(sampleMapBytes, sampleMapAnyKType))
    }

    @Benchmark
    fun decodeMapAny(bh: Blackhole) {
        bh.consume(decoder.decode<Map<String, Any>>(rawMapBytes, mapAnyKType))
    }

}
