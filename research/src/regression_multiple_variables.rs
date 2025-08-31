use dotenv::dotenv;
use nalgebra::{DMatrix, DVector};
use postgres::{Client, Error, NoTls};
use statrs::{distribution::{ContinuousCDF, FisherSnedecor, StudentsT}};
use std::env;

/*

    Living Area(pt: 0.01789824560089648) + Maintainance Cost(pt: 0.007058488523878026) have the greatest impact on pf (0.00014595567114783492) in this dataset

      ┌                      ┐
  │   0.3966809484040241 │
  │  0.01789824560089648 │
  │ 0.007058488523878026 │
  └                      ┘

0.00014595567114783492

*/

struct DegreesOfFreedom {
    regression: i32,
    residual: i32,
    total: i32,
}

impl DegreesOfFreedom {
    fn new(n: i32, k: i32) -> Self {
        let regression = k;
        let total = n - 1;

        DegreesOfFreedom {
            regression: regression,
            residual: total - regression,
            total: total,
        }
    }
}

struct SumOfSquares {
    error: f64,
    regression: f64,
    total: f64,
}

impl SumOfSquares {
    fn new(
        y_vector: nalgebra::Matrix<
            f64,
            nalgebra::Dyn,
            nalgebra::Const<1>,
            nalgebra::VecStorage<f64, nalgebra::Dyn, nalgebra::Const<1>>,
        >,
        y_mean: f64,
        y_hat: nalgebra::Matrix<
            f64,
            nalgebra::Dyn,
            nalgebra::Const<1>,
            nalgebra::VecStorage<f64, nalgebra::Dyn, nalgebra::Const<1>>,
        >,
    ) -> Self {
        let error = (y_vector - &y_hat).map(|val| val.powf(2.0)).sum();

        let regression = (y_hat.map(|val| val - y_mean))
            .map(|val| val.powf(2.0))
            .sum();

        SumOfSquares {
            error: error,
            regression: regression,
            total: error + regression,
        }
    }
}

struct MeanSquared{
    error: f64,
    regression: f64
}

impl MeanSquared{
    fn new(ssr: f64, sse: f64, k: i32, n: i32) -> Self{
        MeanSquared { error: sse / (n - (k + 1)) as f64, regression: ssr / k as f64}
    }
}

fn p_value_of_f(f: f64, numerator: i32, denominator: i32) -> f64{
    let f_dist = FisherSnedecor::new(numerator as f64, denominator as f64).unwrap();

    return 1.0 - f_dist.cdf(f);
}

fn p_value_of_t(t: f64, degrees_of_freedom: i32) -> f64{
    let t_dist = StudentsT::new(0.0, 1.0, degrees_of_freedom as f64).unwrap();

    return 2.0 * (1.0 - t_dist.cdf(t.abs()));
}

fn calculate(mut variables: Vec<Vec<i32>>) {
    let dependent_variables = variables.remove(0);
    let independent_variables = variables;


    let rows = dependent_variables.len();
    let parameters = independent_variables.len();

    let mut x_matrix = DMatrix::<f64>::zeros(rows, parameters + 1);

    for i in 0..rows {
        x_matrix[(i, 0)] = 1.0;

        for j in 0..parameters{
            x_matrix[(i, j + 1)] = independent_variables[j][i] as f64;
        } 
    }

    let mut y_vector = DVector::<f64>::zeros(rows);

    for i in 0..y_vector.len() {
        y_vector[i] = dependent_variables[i] as f64;
    }

    let y_mean = y_vector.mean();

    let x_transpose = x_matrix.transpose();

    let x_transpose_x_inverse = {
        let xtx = &x_transpose * &x_matrix;

        xtx.try_inverse().unwrap()
    };

    let x_transpose_y = &x_transpose * &y_vector;

    let beta_hat = &x_transpose_x_inverse * x_transpose_y;

    let y_hat = x_matrix * &beta_hat;

    let degrees_of_freedom = DegreesOfFreedom::new(rows as i32, parameters as i32);

    let sum_of_squares = SumOfSquares::new(y_vector, y_mean, y_hat);

    let mean_squared = MeanSquared::new(sum_of_squares.regression, sum_of_squares.error, parameters as i32, rows as i32);

    let p_value_of_f = p_value_of_f(mean_squared.regression / mean_squared.error, degrees_of_freedom.regression, degrees_of_freedom.residual);

    let standard_error = sum_of_squares.error / (rows as i32 - parameters as i32) as f64;

    let standard_error_of_beta_hat = DVector::from_vec({
        let mut res = Vec::new();

        for i in 0..beta_hat.len() {
            res.push((standard_error * x_transpose_x_inverse[(i, i)]).sqrt());
        }

        res
    });

    let test_statistic = DVector::from_vec({
        let mut res = Vec::new();

        for i in 0..beta_hat.len() {
            res.push(beta_hat[i] / standard_error_of_beta_hat[i]);
        }

        res
    });

    let p_value_of_t = DVector::from_vec({
        let mut res = Vec::new();

        for i in 0..beta_hat.len() {
            res.push(p_value_of_t(test_statistic[i], degrees_of_freedom.residual));
        }

        res
    });

    print!("{}", p_value_of_t);

    print!("{}", p_value_of_f);
    
}

fn fetch(ids: Vec<i32>) -> Result<Vec<Vec<i32>>, Error> {
    dotenv().ok();

    let postgres_url: String = env::var("POSTGRES_URL").expect("POSTGRES_URL must be set");

    let mut client: Client = Client::connect(&postgres_url, NoTls)?;

    let column_query: Vec<String> = vec![
        "price".to_string(),
        "living_area_metres_squared".to_string(),
        "age_years".to_string(),
        "maintainance_cost_per_month".to_string(),
        "additional_area_metres_squared".to_string(),
        "plot_area_metres_squared".to_string()
    ];

    let mut variables: Vec<Vec<i32>> = vec![Vec::new(); column_query.len()];

    for id in ids {
        let row = client.query_one(
            &format!("SELECT {} FROM villa WHERE id = ($1)", column_query.join(", ")),
            &[&id],
        )?;

        for i in 0..row.len(){
            variables[i].push(row.get(i));
        }
    }

    Ok(variables)
}

pub fn regression_multiple_variables(sample_ids: Vec<i32>) {
    let variables: Vec<Vec<i32>> = match fetch(sample_ids) {
        Ok(res) => res,
        Err(error) => {
            eprintln!("Error fetching properties: {}", error);
            return;
        }
    };

    calculate(variables);
}
