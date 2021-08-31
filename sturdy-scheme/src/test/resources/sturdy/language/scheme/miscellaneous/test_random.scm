(define (map f l)
  (if (null? l)
      l
      (if (pair? l)
          (cons (f (car l)) (map f (cdr l)))
          (error "Cannot map over a non-list"))))


(map (lambda (x) (+ x x)) '(3 3))
