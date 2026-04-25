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
@Fork(1)
open class FastjsonSerdeBenchmark {

    private lateinit var serde: FastjsonSerde
    private lateinit var bytes: ByteArray
    private lateinit var wrappedBytes: ByteArray
    private lateinit var sampleStringBytes: ByteArray
    private lateinit var sampleWrappedBytes: ByteArray
    private lateinit var samplePayloadKType: KType
    private lateinit var wrappedKType: KType
    private lateinit var sampleStringKType: KType
    private lateinit var sampleWrapperKType: KType

    @Setup
    fun setup() {
        serde = FastjsonSerde()
        bytes = """{"id":1,"name":"alice"}""".toByteArray()
        wrappedBytes = """{"data":{"id":1,"name":"alice"}}""".toByteArray()
        sampleStringBytes = """{"data":"alice"}""".toByteArray()
        sampleWrappedBytes = """{"data":{"data":"alice"}}""".toByteArray()
        samplePayloadKType = typeOf<SamplePayload>()
        wrappedKType = typeOf<Wrapper<SamplePayload>>()
        sampleStringKType = typeOf<SampleString>()
        sampleWrapperKType = typeOf<SampleWrapper<SampleString>>()
    }

    @Benchmark
    fun deserializeKClass(bh: Blackhole) {
        bh.consume(serde.deserialize(bytes, SamplePayload::class))
    }

    @Benchmark
    fun deserializeKType(bh: Blackhole) {
        bh.consume(serde.deserialize<SamplePayload>(bytes, samplePayloadKType))
    }

    @Benchmark
    fun deserializeKTypeInline(bh: Blackhole) {
        bh.consume(serde.deserialize<SamplePayload>(bytes, typeOf<SamplePayload>()))
    }

    @Benchmark
    fun deserializeGeneric(bh: Blackhole) {
        bh.consume(serde.deserialize<Wrapper<SamplePayload>>(wrappedBytes, wrappedKType))
    }

    // Compensated: equivalent single-field payload for fair KClass vs KType comparison
    @Benchmark
    fun deserializeSampleStringKClass(bh: Blackhole) {
        bh.consume(serde.deserialize(sampleStringBytes, SampleString::class))
    }

    @Benchmark
    fun deserializeSampleWrapperKType(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleWrapper<SampleString>>(sampleWrappedBytes, sampleWrapperKType))
    }

    data class SamplePayload(val id: Int, val name: String)
    data class Wrapper<T>(val data: T)
    data class SampleString(val data: String)
    data class SampleWrapper<T>(val data: T)
}
