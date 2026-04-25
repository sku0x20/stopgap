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
    private lateinit var samplePayloadKType: KType

    @Setup
    fun setup() {
        serde = FastjsonSerde()
        bytes = """{"id":1,"name":"alice"}""".toByteArray()
        samplePayloadKType = typeOf<SamplePayload>()
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

    data class SamplePayload(val id: Int, val name: String)
}
