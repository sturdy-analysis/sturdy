;; Unsigned LEB128 can have non-minimal length
(module binary
  "\00asm" "\01\00\00\00"
  "\04\04\01"                          ;; Table section with 1 entry
  "\70\00\00"                          ;; no max, minimum 0, funcref
  "\09\09\01"                          ;; Element section with 1 entry
  "\02"                                ;; Element with explicit table index
  "\80\00"                             ;; Table index 0, encoded with 2 bytes
  "\41\00\0b\00\00"                    ;; (i32.const 0) with no elements
)
