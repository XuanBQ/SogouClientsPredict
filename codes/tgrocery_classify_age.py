#!/usr/bin/env python
# coding=utf-8

from tgrocery import Grocery

#grocery = Grocery('age56')
#grocery.train('train4_age_56', ' ')
#grocery.save()

new_grocery = Grocery("age")
new_grocery.load()
predict_result = new_grocery.test('test4_age', ' ')
#print len(predict_result.true_y)
#for i in range(len(predict_result.predicted_y)):
    #print predict_result.predicted_y[i] 
print predict_result
predict_result.show_result()






