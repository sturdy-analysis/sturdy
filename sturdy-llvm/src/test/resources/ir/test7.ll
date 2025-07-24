define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 5, ptr %2, align 4
  %4 = load i32, ptr %2, align 4
  %5 = icmp sgt i32 %4, 10
  br i1 %5, label %6, label %9

6:                                                ; preds = %0
  %7 = load i32, ptr %2, align 4
  %8 = sub nsw i32 %7, 1
  store i32 %8, ptr %3, align 4
  br label %12

9:                                                ; preds = %0
  %10 = load i32, ptr %2, align 4
  %11 = add nsw i32 %10, 1
  store i32 %11, ptr %3, align 4
  br label %12

12:                                               ; preds = %9, %6
  %13 = load i32, ptr %3, align 4
  ret i32 %13
}

