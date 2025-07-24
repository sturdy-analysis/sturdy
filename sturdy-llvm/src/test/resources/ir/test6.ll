define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 1, ptr %2, align 4
  br label %3

3:                                                ; preds = %6, %0
  %4 = load i32, ptr %2, align 4
  %5 = icmp slt i32 %4, 100
  br i1 %5, label %6, label %9

6:                                                ; preds = %3
  %7 = load i32, ptr %2, align 4
  %8 = mul nsw i32 %7, 2
  store i32 %8, ptr %2, align 4
  br label %3, !llvm.loop !6

9:                                                ; preds = %3
  %10 = load i32, ptr %2, align 4
  ret i32 %10
}
