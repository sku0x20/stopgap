package dev.sku20.helidon.ksp

// Java, the fucker, has no built-in write-then-read buffer without copying.
// ByteArrayOutputStream.toByteArray() creates a full copy of all data — 2x memory, wasted CPU.
// For large outputs this is completely unacceptable.
// High-performance frameworks like Netty solved this by writing their own buffer type (ByteBuf).
// The JVM simply has no single abstraction that acts as both OutputStream and InputStream.
// If this ever becomes a problem, can try chunk-based buffer, e.g.
// ArrayDeque<ByteArray> for storage, OutputStream writes into chunks, InputStream reads
// back chunk by chunk and releases each chunk after consumption — zero copy, progressive release.
