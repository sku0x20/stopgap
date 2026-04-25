package com.example.stopgap.serde.fastjson

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
open class FastjsonSerdeBenchmark {

    private lateinit var serde: FastjsonSerde
    private lateinit var sampleStringBytes: ByteArray
    private lateinit var sampleListBytes: ByteArray
    private lateinit var sampleStringKType: KType
    private lateinit var sampleWrapperKType: KType

    @Setup
    fun setup() {
        serde = FastjsonSerde()
        sampleStringBytes = """{"data":"alice"}""".toByteArray()
        sampleListBytes = """{"items":["alice"]}""".toByteArray()
        sampleStringKType = typeOf<SampleString>()
        sampleWrapperKType = typeOf<SampleWrapper<String>>()
    }

    @Benchmark
    fun deserializeKClass(bh: Blackhole) {
        bh.consume(serde.deserialize(sampleStringBytes, SampleString::class))
    }

    @Benchmark
    fun deserializeKType(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleString>(sampleStringBytes, sampleStringKType))
    }

    @Benchmark
    fun deserializeKTypeInline(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleString>(sampleStringBytes, typeOf<SampleString>()))
    }

    // ~7.5x slower than KClass/KType despite cache being hot.
    // Hypothesis (not verified by human — TODO: verify this flow/explanation is correct):
    // fastjson2's ASM codegen for ParameterizedType doesn't specialize field readers per concrete
    // type argument. Instead it stores a resolved ObjectReader<String> in the FieldReader and
    // delegates through it (virtual dispatch) rather than emitting readString() directly.
    // It could monomorphize — T=String is known at reader creation time — but it doesn't.
    // The extra virtual dispatch per field, being less JIT-friendly, accounts for the throughput gap.
    @Benchmark
    fun deserializeGeneric(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleWrapper<String>>(sampleStringBytes, sampleWrapperKType))
    }

    @Benchmark
    fun deserializeList(bh: Blackhole) {
        bh.consume(serde.deserialize(sampleListBytes, SampleList::class))
    }

    data class SampleString(val data: String)
    data class SampleWrapper<T>(val data: T)
    data class SampleList(val items: List<String>)
}
