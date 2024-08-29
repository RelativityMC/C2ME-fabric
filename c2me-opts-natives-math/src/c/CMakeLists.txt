cmake_minimum_required(VERSION 3.25)

set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

project(c2me-opts-natives-math C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_COMPILER clang)
set(CMAKE_CXX_COMPILER clang++)

SET(CMAKE_C_FLAGS_RELWITHDEBINFO "-O3 -g")

add_library(c2me-opts-natives-math SHARED
        exports.c
        system_isa_x86_64.c
        exports_x86_64_nogather.c
        system_isa_aarch64.c
        flibc.c
)

if(CMAKE_SYSTEM_PROCESSOR MATCHES "(x86)|(X86)|(amd64)|(AMD64)")
    set_source_files_properties(exports_x86_64_nogather.c PROPERTIES COMPILE_FLAGS -mno-gather)
endif()

target_include_directories(c2me-opts-natives-math PRIVATE includes/)
target_compile_options(c2me-opts-natives-math PRIVATE $<$<COMPILE_LANGUAGE:C>:-Wall -Wextra -Wpedantic -ffreestanding -fno-math-errno -mprefer-vector-width=512 -ffp-contract=off -Rpass=loop|vect -Rpass-missed=loop|vect -Rpass-analysis=loop|vect -mno-stack-arg-probe -fsave-optimization-record "SHELL:-mllvm -polly" "SHELL:-mllvm -polly-vectorizer=stripmine" "SHELL:-mllvm -extra-vectorizer-passes" "SHELL:-mllvm -slp-vectorize-hor-store" "SHELL:-mllvm -slp-min-tree-size=1" "SHELL:-mllvm -slp-min-reg-size=64" "SHELL:-mllvm -slp-threshold=-1" "SHELL:-mllvm -enable-epilogue-vectorization">)
target_link_options(c2me-opts-natives-math PRIVATE -nostdlib -fuse-ld=lld)
