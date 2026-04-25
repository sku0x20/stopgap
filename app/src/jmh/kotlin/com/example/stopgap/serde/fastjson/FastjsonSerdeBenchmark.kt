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
    private lateinit var sampleStringBytes: ByteArray
    private lateinit var sampleStringKType: KType
    private lateinit var sampleWrapperKType: KType

    @Setup
    fun setup() {
        serde = FastjsonSerde()
        sampleStringBytes = """{"data":"alice"}""".toByteArray()
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

    @Benchmark
    fun deserializeGeneric(bh: Blackhole) {
        bh.consume(serde.deserialize<SampleWrapper<String>>(sampleStringBytes, sampleWrapperKType))
    }

    data class SampleString(val data: String)
    data class SampleWrapper<T>(val data: T)
}
