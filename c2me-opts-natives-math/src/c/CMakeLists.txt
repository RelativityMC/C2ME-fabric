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
        freestanding_workarounds.c
)

if(CMAKE_SYSTEM_PROCESSOR MATCHES "(x86)|(X86)|(amd64)|(AMD64)")
    set_source_files_properties(exports_x86_64_nogather.c PROPERTIES COMPILE_FLAGS -mno-gather)
endif()

target_include_directories(c2me-opts-natives-math PRIVATE includes/)
target_compile_options(c2me-opts-natives-math PRIVATE $<$<COMPILE_LANGUAGE:C>:-Wall -Wextra -Wpedantic -ffreestanding -ffile-prefix-map=${CMAKE_SOURCE_DIR}=. -mprefer-vector-width=512 -Rpass-missed=loop-vectorize -Rpass-analysis=loop-vectorize>)
target_link_options(c2me-opts-natives-math PRIVATE -nostdlib -fuse-ld=lld -nodefaultlibs -v)
