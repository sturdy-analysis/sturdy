define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 100, ptr %2, align 4
  store i32 0, ptr %3, align 4
  br label %4

4:                                                ; preds = %7, %0
  %5 = load i32, ptr %2, align 4
  %6 = icmp sgt i32 %5, 50
  br i1 %6, label %7, label %12

7:                                                ; preds = %4
  %8 = load i32, ptr %2, align 4
  %9 = sub nsw i32 %8, 10
  store i32 %9, ptr %2, align 4
  %10 = load i32, ptr %3, align 4
  %11 = add nsw i32 %10, 1
  store i32 %11, ptr %3, align 4
  br label %4, !llvm.loop !6

12:                                               ; preds = %4
  %13 = load i32, ptr %3, align 4
  ret i32 %13
}