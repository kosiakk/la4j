/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 * 
 */

package org.la4j.decomposition;

import org.la4j.factory.Factory;
import org.la4j.matrix.Matrices;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.functor.AdvancedMatrixPredicate;
import org.la4j.vector.Vector;

/**
 * This class represents Cholesky decomposition of matrices. More details
 * <p>
 * <a href="http://mathworld.wolfram.com/CholeskyDecomposition.html"> here</a>
 * </p>
 */
public class CholeskyDecompositor extends AbstractDecompositor implements MatrixDecompositor {

    public CholeskyDecompositor(Matrix matrix) {
        super(matrix);
    }

    /**
     * Returns the result of Cholesky decomposition of given matrix
     * <p>
     * See <a href="http://mathworld.wolfram.com/CholeskyDecomposition.html">
     * http://mathworld.wolfram.com/CholeskyDecomposition.html</a> for more
     * details.
     * </p>
     *
     * @param factory
     * @return { L }
     */
    @Override
    public Matrix[] decompose(Factory factory) {

        Matrix l = factory.createMatrix(matrix.rows(), matrix.rows());

        for (int j = 0; j < l.rows(); j++) {

            double d = 0.0;

            for (int k = 0; k < j; k++) {

                double s = 0.0;

                for (int i = 0; i < k; i++) {
                    s += l.get(k, i) * l.get(j, i);
                }

                s = (matrix.get(j, k) - s) / l.get(k, k);

                l.set(j, k, s);

                d = d + s * s;
            }

            d = matrix.get(j, j) - d;

            l.set(j, j, Math.sqrt(Math.max(d, 0.0)));

            for (int k = j + 1; k < l.rows(); k++) {
                l.set(j, k, 0.0);
            }
        }

        return new Matrix[] { l };
    }

    /**
     * Checks if the matrix is positive definite
     * <p>
     * See <a href="http://mathworld.wolfram.com/PositiveDefiniteMatrix.html">
     * http://mathworld.wolfram.com/PositiveDefiniteMatrix.html</a> for more
     * details.
     * </p>
     *
     */
    public static class PositiveDefinitePredicate implements AdvancedMatrixPredicate {
        /** @return <code>true</code> if matrix is positive definite */
        public boolean test(Matrix matrix) {

            int n = matrix.rows();

            Matrix l = matrix.blank();

            for (int j = 0; j < n; j++) {

                Vector rowj = l.getRow(j);

                double d = 0.0;
                for (int k = 0; k < j; k++) {

                    Vector rowk = l.getRow(k);
                    double s = 0.0;

                    for (int i = 0; i < k; i++) {
                        s += rowk.get(i) * rowj.get(i);
                    }

                    s = (matrix.get(j, k) - s) / l.get(k, k);

                    rowj.set(k, s);
                    l.setRow(j, rowj);

                    d = d + s * s;
                }

                d = matrix.get(j, j) - d;

                if (d > 0.0)
                    return false;

                l.set(j, j, Math.sqrt(Math.max(d, 0.0)));

                for (int k = j + 1; k < n; k++) {
                    l.set(j, k, 0.0);
                }
            }

            return true;
        }
    }

    @Override
    public boolean applicableTo(Matrix matrix) {
        return matrix.rows() == matrix.columns() &&
               matrix.is(Matrices.SYMMETRIC_MATRIX) &&
               matrix.is(Matrices.POSITIVE_DEFINITE_MATRIX);
    }
}
