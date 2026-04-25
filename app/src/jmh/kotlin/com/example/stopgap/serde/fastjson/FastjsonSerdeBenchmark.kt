package com.example.stopgap.serde.fastjson

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class SampleString(val data: String)
data class SampleWrapper<T>(val data: T)
data class SampleList(val items: List<String>)
data class SampleMap(val entries: Map<String, String>)

// Findings:
// - KClass and KType (cached) perform identically — fastjson2 cache is keyed by Type, and
//   for non-generic types javaType returns the same Class object, hitting the same code path.
// - typeOf() called per iteration costs ~4x vs cached KType — it's the KType creation overhead.
// - User-defined generic classes (SampleWrapper<T>) are ~7.5x slower than plain classes.
//   Hypothesis (not verified by human — TODO: verify this flow/explanation is correct):
//   fastjson2's ASM codegen doesn't monomorphize ParameterizedType — it stores a resolved
//   ObjectReader<String> in the FieldReader and delegates through it (virtual dispatch) rather
//   than emitting readString() directly, even though T=String is known at reader creation time.
// - Standard collections (List, Map) bypass this: fastjson2 has dedicated readers
//   (ObjectReaderImplList, ObjectReaderImplMap) — verified: List ~51k, Map ~40k ops/ms.
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
    private lateinit var sampleMapBytes: ByteArray
    private lateinit var rawMapBytes: ByteArray
    private lateinit var wrappedMapAnyBytes: ByteArray
    private lateinit var sampleStringKType: KType
    private lateinit var sampleWrapperKType: KType
    private lateinit var sampleWrapperAnyKType: KType
    private lateinit var mapAnyKType: KType

    @Setup
    fun setup() {
        serde = FastjsonSerde()
        sampleStringBytes = """{"data":"alice"}""".toByteArray()
        sampleListBytes = """{"items":["alice"]}""".toByteArray()
        sampleMapBytes = """{"entries":{"key":"alice"}}""".toByteArray()
        rawMapBytes = """{"key":"alice"}""".toByteArray()
        wrappedMapAnyBytes = """{"data":{"key":"alice"}}""".toByteArray()
        sampleStringKType = typeOf<SampleString>()
        sampleWrapperKType = typeOf<SampleWrapper<String>>()
        sampleWrapperAnyKType = typeOf<SampleWrapper<Map<String, Any>>>()
        mapAnyKType = typeOf<Map<String, Any>>()
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

    @Benchmark
    fun deserializeGeneric(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleWrapper<String>>(sampleStringBytes, sampleWrapperKType))
    }

    @Benchmark
    fun deserializeList(bh: Blackhole) {
        bh.consume(serde.deserialize(sampleListBytes, SampleList::class))
    }

    @Benchmark
    fun deserializeMap(bh: Blackhole) {
        bh.consume(serde.deserialize(sampleMapBytes, SampleMap::class))
    }

    @Benchmark
    fun deserializeGenericAny(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleWrapper<Map<String, Any>>>(wrappedMapAnyBytes, sampleWrapperAnyKType))
    }

    @Benchmark
    fun deserializeMapAny(bh: Blackhole) {
        bh.consume(serde.deserialize<Map<String, Any>>(rawMapBytes, mapAnyKType))
    }

}
