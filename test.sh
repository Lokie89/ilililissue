#!/bin/bash

var_command=${1}
var_epic=${2}

if [ ${var_command} = "qa" ]
then
  git checkout feature/${var_epic}
  git stash -u
  git remote update
  git pull origin feature/${var_epic}
  git checkout master
  git pull origin master
  git branch release/${var_epic}
  git checkout release/${var_epic}
  git pull origin release/${var_epic}
  git rebase -i master
  git merge --no-ff feature/${var_epic}
  git branch -D feature/${var_epic}
  git push origin release/${var_epic}
fi