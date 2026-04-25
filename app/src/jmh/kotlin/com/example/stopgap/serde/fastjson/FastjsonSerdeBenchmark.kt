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
data class SampleMapAny(val entries: Map<String, Any>)

// Findings:
// - KClass and KType (cached) perform identically — fastjson2 cache is keyed by Type, and
//   for non-generic types javaType returns the same Class object, hitting the same code path.
//   ~65-70k ops/ms.
// - typeOf() called per iteration costs ~4x vs cached KType (~17k ops/ms) — KType creation overhead.
// - User-defined generic classes (SampleWrapper<T>) are ~7.5x slower (~8.5k ops/ms).
//   Hypothesis: fastjson2's ASM codegen doesn't monomorphize ParameterizedType — it stores a
//   resolved ObjectReader<String> in the FieldReader and delegates through it (virtual dispatch)
//   rather than emitting readString() directly, even though T=String is known at reader creation time.
// - Standard collections (List, Map) bypass this: fastjson2 has dedicated readers
//   (ObjectReaderImplList, ObjectReaderImplMap) — verified: List ~51k, Map ~40k ops/ms.
// - Map<String, Any> requires runtime type detection per value — ~10k ops/ms.
// - Concrete class with a Map<String, Any> field (SampleMapAny) stacks two penalties:
//   (1) ParameterizedType field reader — virtual dispatch for the entries field itself,
//   (2) Any value type — runtime type detection per map entry.
//   Result: ~5.7k ops/ms, worse than either penalty alone.
//
// Bottomline: avoid generics in hot deserialization paths — penalties stack. Prefer concrete
// types with known fields so fastjson2 can generate optimized ASM readers.
//
// TODO: verify all flow paths above — none confirmed by reading fastjson2 source directly.
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
    private lateinit var sampleAnyMapBytes: ByteArray
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
        sampleAnyMapBytes = """{"data":{"key":"alice"}}""".toByteArray()
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
    fun deserializeSampleMapAny(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleMapAny>(sampleAnyMapBytes, sampleWrapperAnyKType))
    }

    @Benchmark
    fun deserializeMapAny(bh: Blackhole) {
        bh.consume(serde.deserialize<Map<String, Any>>(rawMapBytes, mapAnyKType))
    }

}
