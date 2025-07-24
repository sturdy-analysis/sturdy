define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 1, ptr %2, align 4
  store i32 0, ptr %3, align 4
  br label %4

4:                                                ; preds = %7, %0
  %5 = load i32, ptr %3, align 4
  %6 = icmp slt i32 %5, 5
  br i1 %6, label %7, label %13

7:                                                ; preds = %4
  %8 = load i32, ptr %2, align 4
  %9 = mul nsw i32 2, %8
  %10 = add nsw i32 %9, 1
  store i32 %10, ptr %2, align 4
  %11 = load i32, ptr %3, align 4
  %12 = add nsw i32 %11, 1
  store i32 %12, ptr %3, align 4
  br label %4, !llvm.loop !6

13:                                               ; preds = %4
  %14 = load i32, ptr %2, align 4
  ret i32 %14
}
