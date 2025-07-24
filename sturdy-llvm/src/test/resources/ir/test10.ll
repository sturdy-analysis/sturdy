define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 0, ptr %2, align 4
  store i32 0, ptr %3, align 4
  br label %4

4:                                                ; preds = %14, %0
  %5 = load i32, ptr %2, align 4
  %6 = icmp slt i32 %5, 10
  br i1 %6, label %7, label %17

7:                                                ; preds = %4
  %8 = load i32, ptr %2, align 4
  %9 = srem i32 %8, 2
  %10 = icmp eq i32 %9, 0
  br i1 %10, label %11, label %14

11:                                               ; preds = %7
  %12 = load i32, ptr %3, align 4
  %13 = add nsw i32 %12, 3
  store i32 %13, ptr %3, align 4
  br label %14

14:                                               ; preds = %11, %7
  %15 = load i32, ptr %2, align 4
  %16 = add nsw i32 %15, 1
  store i32 %16, ptr %2, align 4
  br label %4, !llvm.loop !6

17:                                               ; preds = %4
  %18 = load i32, ptr %3, align 4
  ret i32 %18
}
