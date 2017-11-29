# setup.py
from setuptools import setup, find_packages

setup(name='xmlstm',
  version='0.1',
  packages=find_packages(),
  description='example to run keras s2s model on gcloud ml-engine',
  author='rasmus buchmann',
  author_email='rasmus.buchmann@example.com',
  license='MIT',
  install_requires=[
      'keras',
      'h5py'
  ],
  zip_safe=False)
