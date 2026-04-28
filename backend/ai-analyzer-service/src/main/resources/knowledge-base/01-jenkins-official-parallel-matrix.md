# Jenkins Pipeline Syntax - Parallel and Matrix

**Source:** https://www.jenkins.io/doc/book/pipeline/syntax/#parallel  
**Date d'extraction:** 2025-01-17

---

## Parallel

Stages in Declarative Pipeline may have a parallel section containing a list of nested stages to be run in parallel.

A stage must have one and only one of steps, stages, parallel, or matrix. It is not possible to nest a parallel or matrix block within a stage directive if that stage directive is nested within a parallel or matrix block itself. However, a stage directive within a parallel or matrix block can use all other functionality of a stage, including agent, tools, when, etc.

In addition, you can force your parallel stages to all be aborted when any one of them fails, by adding failFast true to the stage containing the parallel. Another option for adding failfast is adding an option to the pipeline definition: parallelsAlwaysFailFast().

### Example 25. Parallel Stages, Declarative Pipeline

```groovy
pipeline {
    agent any
    stages {
        stage('Non-Parallel Stage') {
            steps {
                echo 'This stage will be executed first.'
            }
        }
        stage('Parallel Stage') {
            when {
                branch 'master'
            }
            failFast true
            parallel {
                stage('Branch A') {
                    agent {
                        label "for-branch-a"
                    }
                    steps {
                        echo "On Branch A"
                    }
                }
                stage('Branch B') {
                    agent {
                        label "for-branch-b"
                    }
                    steps {
                        echo "On Branch B"
                    }
                }
                stage('Branch C') {
                    agent {
                        label "for-branch-c"
                    }
                    stages {
                        stage('Nested 1') {
                            steps {
                                echo "In stage Nested 1 within Branch C"
                            }
                        }
                        stage('Nested 2') {
                            steps {
                                echo "In stage Nested 2 within Branch C"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Example 26. parallelsAlwaysFailFast

```groovy
pipeline {
    agent any
    options {
        parallelsAlwaysFailFast()
    }
    stages {
        stage('Non-Parallel Stage') {
            steps {
                echo 'This stage will be executed first.'
            }
        }
        stage('Parallel Stage') {
            when {
                branch 'master'
            }
            parallel {
                stage('Branch A') {
                    agent {
                        label "for-branch-a"
                    }
                    steps {
                        echo "On Branch A"
                    }
                }
                stage('Branch B') {
                    agent {
                        label "for-branch-b"
                    }
                    steps {
                        echo "On Branch B"
                    }
                }
                stage('Branch C') {
                    agent {
                        label "for-branch-c"
                    }
                    stages {
                        stage('Nested 1') {
                            steps {
                                echo "In stage Nested 1 within Branch C"
                            }
                        }
                        stage('Nested 2') {
                            steps {
                                echo "In stage Nested 2 within Branch C"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## Matrix

Stages in Declarative Pipeline may have a matrix section defining a multi-dimensional matrix of name-value combinations to be run in parallel. We'll refer these combinations as "cells" in a matrix. Each cell in a matrix can include one or more stages to be run sequentially using the configuration for that cell.

A stage must have one and only one of steps, stages, parallel, or matrix. It is not possible to nest a parallel or matrix block within a stage directive if that stage directive is nested within a parallel or matrix block itself. However, a stage directive within a parallel or matrix block can use all other functionality of a stage, including agent, tools, when, etc.

In addition, you can force your matrix cells to all be aborted when any one of them fails, by adding failFast true to the stage containing the matrix. Another option for adding failfast is adding an option to the pipeline definition: parallelsAlwaysFailFast().

The matrix section must include an axes section and a stages section. The axes section defines the values for each axis in the matrix. The stages section defines a list of stages to run sequentially in each cell. A matrix may have an excludes section to remove invalid cells from the matrix. Many of the directives available on stage, including agent, tools, when, etc., can also be added to matrix to control the behavior of each cell.

### axes

The axes section specifies one or more axis directives. Each axis consists of a name and a list of values. All the values from each axis are combined with the others to produce the cells.

#### Example 27. One-axis with 3 cells

```groovy
matrix {
    axes {
        axis {
            name 'PLATFORM'
            values 'linux', 'mac', 'windows'
        }
    }
    // ...
}
```

#### Example 28. Two-axis with 12 cells (three by four)

```groovy
matrix {
    axes {
        axis {
            name 'PLATFORM'
            values 'linux', 'mac', 'windows'
        }
        axis {
            name 'BROWSER'
            values 'chrome', 'edge', 'firefox', 'safari'
        }
    }
    // ...
}
```

#### Example 29. Three-axis matrix with 24 cells (three by four by two)

```groovy
matrix {
    axes {
        axis {
            name 'PLATFORM'
            values 'linux', 'mac', 'windows'
        }
        axis {
            name 'BROWSER'
            values 'chrome', 'edge', 'firefox', 'safari'
        }
        axis {
            name 'ARCHITECTURE'
            values '32-bit', '64-bit'
        }
    }
    // ...
}
```