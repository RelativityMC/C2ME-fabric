#pragma once

#define TARGET_IMPL_ARCH(suffix, func_prefix, func_ret, func_call) \
  func_ret func_prefix##_##suffix func_call

#ifdef __x86_64__

#ifdef GATHER_DISABLED
#define TARGET_IMPL(func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=haswell"))) TARGET_IMPL_ARCH(avx2, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=skylake-avx512"))) TARGET_IMPL_ARCH(avx512skx, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=icelake-server"))) TARGET_IMPL_ARCH(avx512icl, func_prefix, func_ret, func_call)
#else
#define TARGET_IMPL(func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=x86-64"))) TARGET_IMPL_ARCH(sse2, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=x86-64-v2"))) TARGET_IMPL_ARCH(sse4_2, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=sandybridge"))) TARGET_IMPL_ARCH(avx, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=alderlake"))) TARGET_IMPL_ARCH(avx2adl, func_prefix, func_ret, func_call) \
  __attribute__((pure, target("arch=sapphirerapids"))) TARGET_IMPL_ARCH(avx512spr, func_prefix, func_ret, func_call)
#endif

#else

#define TARGET_IMPL(func_prefix, func_ret, func_call) \
  __attribute__((pure)) TARGET_IMPL_ARCH(generic, func_prefix, func_ret, func_call)

#endif

