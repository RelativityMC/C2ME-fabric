cmake_minimum_required(VERSION 3.25)

set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

project(c2me-opts-natives-math C)

set(CMAKE_C_STANDARD 23)
set(CMAKE_C_COMPILER clang)

SET(CMAKE_C_FLAGS_RELWITHDEBINFO "-O3 -g")

set(CMAKE_C_STANDARD_LIBRARIES "")
set(CMAKE_CXX_STANDARD_LIBRARIES "")

add_library(c2me-opts-natives-math SHARED
        exports_x86_64_sse2.c
        exports_x86_64_sse4_2.c
        exports_x86_64_avx.c
        exports_x86_64_avx2.c
        exports_x86_64_avx2vnni.c
        exports_x86_64_avx512skx.c
        exports_x86_64_avx512icl.c
        exports_x86_64_avx512spr.c
        exports_x86_64_avx10_2dmr.c
        exports_x86_64_avx10_2nvl.c
        system_isa_x86_64.c
        system_isa_aarch64.c
        flibc.c
)

if(CMAKE_SYSTEM_PROCESSOR MATCHES "(x86)|(X86)|(amd64)|(AMD64)")
    set_source_files_properties(exports_x86_64_sse2.c PROPERTIES COMPILE_FLAGS "-march=x86-64 -D UARCH_SUFFIX=sse2")
    set_source_files_properties(exports_x86_64_sse4_2.c PROPERTIES COMPILE_FLAGS "-march=x86-64-v2 -D UARCH_SUFFIX=sse4_2")
    set_source_files_properties(exports_x86_64_avx.c PROPERTIES COMPILE_FLAGS "-march=sandybridge -D UARCH_SUFFIX=avx")
    set_source_files_properties(exports_x86_64_avx2.c PROPERTIES COMPILE_FLAGS "-march=haswell -D UARCH_SUFFIX=avx2 -mno-gather -mno-scatter")
    set_source_files_properties(exports_x86_64_avx2vnni.c PROPERTIES COMPILE_FLAGS "-march=alderlake -D UARCH_SUFFIX=avx2vnni")
    set_source_files_properties(exports_x86_64_avx512skx.c PROPERTIES COMPILE_FLAGS "-march=skylake-avx512 -D UARCH_SUFFIX=avx512skx -mno-gather -mno-scatter")
    set_source_files_properties(exports_x86_64_avx512icl.c PROPERTIES COMPILE_FLAGS "-march=icelake-server -D UARCH_SUFFIX=avx512icl")
    set_source_files_properties(exports_x86_64_avx512spr.c PROPERTIES COMPILE_FLAGS "-march=sapphirerapids -D UARCH_SUFFIX=avx512spr")
    set_source_files_properties(exports_x86_64_avx10_2dmr.c PROPERTIES COMPILE_FLAGS "-march=diamondrapids -D UARCH_SUFFIX=avx10_2dmr")
    set_source_files_properties(exports_x86_64_avx10_2nvl.c PROPERTIES COMPILE_FLAGS "-march=novalake -D UARCH_SUFFIX=avx10_2nvl")
endif()

execute_process(COMMAND llvm-config --prefix OUTPUT_VARIABLE LLVM_PREFIX OUTPUT_STRIP_TRAILING_WHITESPACE)

target_include_directories(c2me-opts-natives-math PRIVATE includes/)
target_compile_options(c2me-opts-natives-math PRIVATE $<$<COMPILE_LANGUAGE:C>:-Wall -Wextra -Wpedantic -Wno-gnu-statement-expression -ffreestanding -Wno-gnu-statement-expression-from-macro-expansion -ffile-prefix-map=${CMAKE_SOURCE_DIR}=. -fdebug-compilation-dir=. -fdebug-prefix-map=${CMAKE_SOURCE_DIR}=. -fdebug-prefix-map=${LLVM_PREFIX}=.../llvm-prefix -fno-math-errno -mprefer-vector-width=512 -ffp-contract=off -mno-stack-arg-probe -mstack-alignment=64 -mstackrealign -fsave-optimization-record "SHELL:-mllvm -extra-vectorizer-passes" "SHELL:-mllvm -slp-vectorize-hor-store" "SHELL:-mllvm -slp-min-tree-size=2" "SHELL:-mllvm -slp-min-reg-size=64" "SHELL:-mllvm -slp-threshold=-1" "SHELL:-mllvm -enable-epilogue-vectorization" >)
target_link_options(c2me-opts-natives-math PRIVATE -v -nostdlib -fuse-ld=lld -ffile-prefix-map=${CMAKE_SOURCE_DIR}=. -fdebug-compilation-dir=. -fdebug-prefix-map=${CMAKE_SOURCE_DIR}=. -fdebug-prefix-map=${LLVM_PREFIX}=.../llvm-prefix)

