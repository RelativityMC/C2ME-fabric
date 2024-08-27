typedef int make_iso_compilers_happy;

#ifdef WIN32

// ld.lld: error: <root>: undefined symbol: DllMainCRTStartup
int __stdcall DllMainCRTStartup(void* instance, unsigned reason, void* reserved)
{
  (void) instance;
  (void) reason;
  (void) reserved;
  return 1;
}

// ld.lld: error: undefined symbol: _fltused
int _fltused = 0;

// ld.lld: error: undefined symbol: abort
void abort(void)
{
  __builtin_trap();
}

// ld.lld: error: undefined symbol: truncf
float truncf(float x)
{
  return __builtin_truncf(x);
}

#endif // WIN32

// ld.lld: error: undefined symbol: floor
double floor(double x) {
  return __builtin_floor(x);
}

double fmodf(float x, float y) {
  return __builtin_fmodf(x, y);
}
