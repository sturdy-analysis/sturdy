(define (set-cdr! location value)
        (set! location (cons (car location) value)))

(define (set-car! location value)
        (set! location (cons value (cdr location))))

(define (equal? x y)
       (if (eq? x y)
           #t
           (if (and (null? x) (null? y))
               #t
               (if (and (cons? x) (cons? y))
                   (and (equal? (car x) (car y)) (equal? (cdr x) (cdr y)))
                   #f))))

(define (list? l)
        (if (cons? l) (list? (cdr l)) (null? l)))
