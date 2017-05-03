# coding=utf-8
import numpy as np
import jieba

from sklearn import metrics
from sklearn.neighbors import KNeighborsClassifier
from sklearn.feature_extraction.text import CountVectorizer  # sklearn中的文本特征提取组件中，导入特征向量计数函数
from sklearn.feature_extraction.text import TfidfTransformer  # sklearn中的文本特征提取组件中，导入词频统计函数
from sklearn.naive_bayes import *

# load data

def FetchDataset(filename):
    labels = []
    texts  = []
    file = open(filename)
    for line in file:
        l,t = line.split(' ', 1)
        labels.append(l)
        seg = jieba.cut(t)
        segs = [t for t in seg if len(t) > 1]
        text = ' '.join(segs)
        texts.append(text)
    return labels, texts


train_Y, train_X = FetchDataset('train4_gender')
test_Y,  test_X  = FetchDataset('test4_gender')

count_vect = CountVectorizer()  # 特征向量计数函数
X_train_counts = count_vect.fit_transform(train_X)  # 对文本进行特征向量处理

#tf_transformer = TfidfTransformer(use_idf=False).fit(X_train_counts)  # 建立词频统计函数,注意这里idf=False
#print tf_transformer  # 输出函数属性 TfidfTransformer(norm=u'l2', smooth_idf=True, sublinear_tf=False, use_idf=False)
#print '-----'
#X_train_tf = tf_transformer.transform(X_train_counts)  # 使用函数对文本文档进行tf-idf频率计算
#print X_train_tf
#print '-----'

tfidf_transformer = TfidfTransformer()  # 这里使用的是tf-idf
X_train_tfidf = tfidf_transformer.fit_transform(X_train_counts)


# convert test data
X_test_counts = count_vect.transform(test_X)  # 构建文档计数
X_test_tfidf = tfidf_transformer.transform(X_test_counts)  # 构建文档tfidf

# NB
#clf = BernoulliNB(alpha=0.01)  # 加载多项式函数
#x_clf = clf.fit(X_train_tfidf, train_Y)  # 构造基于数据的分类器
#print x_clf  # 分类器属性：MultinomialNB(alpha=1.0, class_prior=None, fit_prior=True)
#print '-----'
#predicted_nb_Y = clf.predict(X_test_tfidf)  # 预测文档
#print np.mean(predicted_nb_Y == test_Y)


# KNN
# fit a k-nearest neighbor model to the data
knn_model = KNeighborsClassifier(n_neighbors = 200)
knn_model.fit(X_train_tfidf, train_Y)
print(knn_model)
print '---------'

# make predictions
knn_predicted_Y = knn_model.predict(X_test_tfidf)

# summarize the fit of the model
print(metrics.classification_report(test_Y, knn_predicted_Y))
print(metrics.confusion_matrix(test_Y, knn_predicted_Y))
