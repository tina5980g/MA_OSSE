import ensurepip

print('ensuring pip')
ensurepip._main()
import pip

print('installing catboost')
pip.main(['install', 'catboost'])

print('installing our own dependencies')
pip.main(['install', 'pandas', 'scikit-learn'])

print('All Good!')

